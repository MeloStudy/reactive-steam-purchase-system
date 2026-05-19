package com.melodev.storeservice.service;

import com.melodev.storeservice.client.CatalogueClient;
import com.melodev.storeservice.client.GameResponse;
import com.melodev.storeservice.exceptions.GameAlreadyInCartException;
import com.melodev.storeservice.exceptions.GameNotAvailableException;
import com.melodev.storeservice.exceptions.GameNotFoundException;
import com.melodev.storeservice.model.Cart;
import com.melodev.storeservice.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.melodev.storeservice.config.ContextUser.USER_DEFAULT_ID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CatalogueClient catalogueClient;

    public Flux<Cart> getAllCarts() {
        return cartRepository.findAll();
    }

    public Mono<Cart> getActiveCart() {
        return cartRepository.findByUserIdAndStatus(USER_DEFAULT_ID, "ACTIVE");
    }

    public Mono<Cart> addItemToCart(String itemId) {
        return catalogueClient.getGame(itemId)
                .filter(game -> !game.isDummy())
                .switchIfEmpty(Mono.error(new GameNotFoundException(itemId))) // evaluate to simplify, just propagating the error and not returning dummy
                .filter(GameResponse::isAvailable)
                .switchIfEmpty(Mono.error(new GameNotAvailableException(itemId)))
                .flatMap(game -> this.getActiveCart()
                        .switchIfEmpty(Mono.defer(() -> {
                            log.info("Active cart not found -> Creating new active cart");
                            return cartRepository.save(Cart.perDefault());
                        }))
                        .flatMap(cart -> {
                            if (cart.isInCart(itemId)) return Mono.error(new GameAlreadyInCartException(itemId));

                            return cartRepository.addItem(cart.getCartId(), itemId);
                        })
                        .doOnSuccess(c -> log.info("Added element {} to cart {}", game.getId(), c))
                );
    }
}
