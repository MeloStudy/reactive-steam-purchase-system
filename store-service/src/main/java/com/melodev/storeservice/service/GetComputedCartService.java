package com.melodev.storeservice.service;

import com.melodev.storeservice.client.CatalogueClient;
import com.melodev.storeservice.client.GameResponse;
import com.melodev.storeservice.model.Cart;
import com.melodev.storeservice.model.CartItem;
import com.melodev.storeservice.repository.CartRepository;
import com.melodev.storeservice.service.dtos.CartItemResponse;
import com.melodev.storeservice.service.dtos.CartResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static com.melodev.storeservice.config.ContextUser.USER_DEFAULT_ID;

@Service
@Slf4j
@RequiredArgsConstructor
public class GetComputedCartService {
    private final CartRepository cartRepository;
    private final LibraryService libraryService;
    private final CatalogueClient catalogueClient;

    private static final String STATUS_SERVICE_UNAVAILABLE = "SERVICE_UNAVAILABLE";
    private static final String STATUS_NOT_FOUND = "NOT_FOUND";
    private static final String STATUS_NOT_AVAILABLE = "NOT_AVAILABLE";
    private static final String STATUS_OWNED = "OWNED";
    private static final String STATUS_OK = "OK";
    private static final String STATUS_BLOCKED = "BLOCKED";

    private static final String MSG_REMOVE_OWNED = "Remove owned games";
    private static final String MSG_REMOVE_NOT_FOUND = "Remove games not found in catalogue";
    private static final String MSG_SERVICE_UNAVAILABLE = "Catalogue service unavailable";
    private static final String MSG_REMOVE_UNAVAILABLE = "Remove unavailable games";

    // Record to carry the result or error of each catalog service call
    private record CatalogueResult(GameResponse game, Throwable error) {
    }

    public Mono<CartResponse> getComputedCart() {
        return cartRepository.findByUserIdAndStatus(USER_DEFAULT_ID, "ACTIVE")
                .flatMap(cart -> {
                    if (cart.getItems() == null || cart.getItems().isEmpty()) {
                        return Mono.just(CartResponse.defaultEmpty(cart));
                    }

                    return Flux.fromIterable(cart.getItems())
                            .flatMap(this::enrichCartItem)
                            .collectList()
                            .map(enrichedItems -> buildCartResponse(cart, enrichedItems));
                })
                .switchIfEmpty(Mono.just(CartResponse.draft(USER_DEFAULT_ID)));
    }

    private Mono<CartItemResponse> enrichCartItem(CartItem item) {
        String gameId = item.getGameId();

        // wrap response in a record that carry the result or error, for not found we recieve a dummy game
        Mono<CatalogueResult> retrievingFromCatalogue = catalogueClient.getGame(gameId)
                .map(game -> new CatalogueResult(game, null))
                .onErrorResume(ex -> Mono.just(new CatalogueResult(null, ex)));

        // simplification: in case of error, assume is not present in library
        Mono<Boolean> validatingInLibrary = libraryService.isGameInLibrary(gameId)
                .onErrorReturn(false);

        return Mono.zip(retrievingFromCatalogue, validatingInLibrary)
                .map(tuple -> {
                    CatalogueResult catResult = tuple.getT1();
                    boolean isGameOwned = tuple.getT2();
                    return buildCartItemResponse(gameId, catResult, isGameOwned);
                });
    }

    // Simplification: Didn't use complex chain of responsibility or other patterns
    private CartItemResponse buildCartItemResponse(String gameId, CatalogueResult catResult, boolean isGameOwned) {
        // 1. External catalog service connectivity error or timeout
        if (catResult.error() != null) {
            return CartItemResponse.builder()
                    .id(gameId)
                    .originalPrice(0.0)
                    .discount(0.0)
                    .finalPrice(0.0)
                    .status(STATUS_SERVICE_UNAVAILABLE)
                    .disclaimer("Catalogue service temporarily unavailable")
                    .build();
        }

        GameResponse game = catResult.game();

        // 2. Business error: Game not found (404 is mapped to dummy response)
        if (game.isDummy()) {
            return CartItemResponse.builder()
                    .id(gameId)
                    .originalPrice(0.0)
                    .discount(0.0)
                    .finalPrice(0.0)
                    .status(STATUS_NOT_FOUND)
                    .disclaimer("Game not found in catalogue")
                    .build();
        }

        // 3. Game not available (Banned, location restricted, or not published)
        if (!game.isAvailable()) {
            return CartItemResponse.builder()
                    .id(gameId)
                    .originalPrice(0.0)
                    .discount(0.0)
                    .finalPrice(0.0)
                    .status(STATUS_NOT_AVAILABLE)
                    .disclaimer("Not available product")
                    .build();
        }

        // Calculate normal pricing with defensive bounds checking
        double originalPrice = Math.max(0.0, game.getPrice());

        // Clamp discount percentage to [0.0, 100.0] range
        double discountPercentage = Math.clamp(game.getDiscountPercentage(), 0.0, 100.0);

        double discount = game.isActiveDiscount() ? (originalPrice * (discountPercentage / 100.0)) : 0.0;
        double finalPrice = Math.max(0.0, originalPrice - discount);

        // 4. Game already purchased (OWNED) - Does not block price calculation
        if (isGameOwned) {
            return CartItemResponse.builder()
                    .id(gameId)
                    .originalPrice(originalPrice)
                    .discount(discount)
                    .finalPrice(finalPrice)
                    .status(STATUS_OWNED)
                    .disclaimer("Already owned")
                    .build();
        }

        // 5. Successful path (OK)
        return CartItemResponse.builder()
                .id(gameId)
                .originalPrice(originalPrice)
                .discount(discount)
                .finalPrice(finalPrice)
                .status(STATUS_OK)
                .build();
    }

    private CartResponse buildCartResponse(Cart cart, List<CartItemResponse> enrichedItems) {
        List<String> validations = new ArrayList<>();
        String cartStatus = STATUS_OK;

        // Evaluate item states to calculate global cart status and validation disclaimers
        for (CartItemResponse item : enrichedItems) {
            String itemStatus = item.getStatus();

            switch (itemStatus) {
                case STATUS_OWNED -> validations.add(MSG_REMOVE_OWNED);
                case STATUS_NOT_FOUND -> {
                    cartStatus = STATUS_BLOCKED;
                    validations.add(MSG_REMOVE_NOT_FOUND);
                }
                case STATUS_SERVICE_UNAVAILABLE -> {
                    cartStatus = STATUS_BLOCKED;
                    validations.add(MSG_SERVICE_UNAVAILABLE);
                }
                case STATUS_NOT_AVAILABLE -> {
                    cartStatus = STATUS_BLOCKED;
                    validations.add(MSG_REMOVE_UNAVAILABLE);
                }
                default -> { // No validation or blocking needed for other statuses (e.g., STATUS_OK)
                }
            }
        }

        // Calculate overall monetary total (validations do not block total calculation)
        double total = enrichedItems.stream()
                .mapToDouble(CartItemResponse::getFinalPrice)
                .sum();

        // Checkout is allowed only if status is NOT BLOCKED
        boolean checkoutAllowed = !STATUS_BLOCKED.equals(cartStatus);

        return CartResponse.builder()
                .cartId(cart.getCartId())
                .userId(cart.getUserId())
                .items(enrichedItems)
                .total(total)
                .checkoutAllowed(checkoutAllowed)
                .status(cartStatus)
                .validations(validations.stream().distinct().toList())
                .build();
    }
}
