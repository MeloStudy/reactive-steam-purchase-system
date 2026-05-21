package com.melodev.storeservice.service;

import com.melodev.storeservice.client.CatalogueClient;
import com.melodev.storeservice.client.GameResponse;
import com.melodev.storeservice.exceptions.GameAlreadyInCartException;
import com.melodev.storeservice.exceptions.GameNotAvailableException;
import com.melodev.storeservice.exceptions.GameNotFoundException;
import com.melodev.storeservice.model.Cart;
import com.melodev.storeservice.model.CartItem;
import com.melodev.storeservice.repository.CartRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static com.melodev.storeservice.config.ContextUser.USER_DEFAULT_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

// Simplification: Didn't use a more complex data test generator like object mother or data builder
@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CatalogueClient catalogueClient;

    @InjectMocks
    private CartService cartService;

    @Test
    void shouldThrowGameNotFoundException() {
        // Arrange
        String itemId = "GAME-404";
        doReturn(Mono.just(GameResponse.dummy())).when(catalogueClient).getGame(itemId);

        // Act
        Mono<Cart> result = cartService.addItemToCart(itemId);

        // Assert
        StepVerifier.create(result)
                .expectError(GameNotFoundException.class)
                .verify();

        verify(catalogueClient).getGame(itemId);
        verifyNoInteractions(cartRepository);
    }

    @Test
    void shouldThrowGameNotAvailableException() {
        // Arrange
        String itemId = "GAME-UNAVAILABLE";
        GameResponse unavailableGame = createGameResponse(itemId, false);

        doReturn(Mono.just(unavailableGame)).when(catalogueClient).getGame(itemId);

        // Act
        Mono<Cart> result = cartService.addItemToCart(itemId);

        // Assert
        StepVerifier.create(result)
                .expectError(GameNotAvailableException.class)
                .verify();

        verify(catalogueClient).getGame(itemId);
        verifyNoInteractions(cartRepository);
    }

    @Test
    void shouldThrowGameAlreadyInCartException() {
        // Arrange
        String itemId = "GAME-001";
        GameResponse validGame = createGameResponse(itemId, true);
        Cart existingCart = createActiveCart("CART-123", itemId);

        doReturn(Mono.just(validGame))
                .when(catalogueClient).getGame(itemId);
        doReturn(Mono.just(existingCart))
                .when(cartRepository).findByUserIdAndStatus(anyString(), anyString());

        // Act
        Mono<Cart> result = cartService.addItemToCart(itemId);

        // Assert
        StepVerifier.create(result)
                .expectError(GameAlreadyInCartException.class)
                .verify();

        verify(catalogueClient).getGame(itemId);
        verify(cartRepository).findByUserIdAndStatus(anyString(), anyString());
        verify(cartRepository, never()).save(any(Cart.class));
        verify(cartRepository, never()).addItem(anyString(), anyString());
    }

    @Test
    void shouldCreateNewCart_andAddFirstItem() {
        // Arrange
        String newGameId = "GAME-VALID";
        String newCartId = "CART-NEW";

        GameResponse validGame = createGameResponse(newGameId, true);
        Cart newCart = createActiveCart(newCartId);
        Cart updatedCart = createActiveCart(newCartId, newGameId);

        doReturn(Mono.just(validGame))
                .when(catalogueClient).getGame(newGameId);
        doReturn(Mono.empty())
                .when(cartRepository).findByUserIdAndStatus(anyString(), anyString());
        doReturn(Mono.just(newCart))
                .when(cartRepository).save(any(Cart.class));
        doReturn(Mono.just(updatedCart))
                .when(cartRepository).addItem(anyString(), anyString());

        // Act
        Mono<Cart> result = cartService.addItemToCart(newGameId);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(cart -> {
                    assertEquals(newCartId, cart.getCartId());
                    assertEquals(1, cart.getItems().size());
                    assertEquals(newGameId, cart.getItems().getFirst().getGameId());
                    return true;
                })
                .verifyComplete();

        verify(catalogueClient).getGame(newGameId);
        verify(cartRepository).findByUserIdAndStatus(USER_DEFAULT_ID, "ACTIVE");
        verify(cartRepository).save(any(Cart.class));
        verify(cartRepository).addItem(newCart.getCartId(), newGameId);
    }

    @Test
    void shouldAddItem_whenActiveCartExists() {
        // Arrange
        String itemId = "GAME-VALID";
        String existingCartId = "CART-123";

        GameResponse validGame = createGameResponse(itemId, true);
        Cart existingCart = createActiveCart(existingCartId);
        Cart updatedCart = createActiveCart(existingCartId, itemId);

        doReturn(Mono.just(validGame))
                .when(catalogueClient).getGame(itemId);
        doReturn(Mono.just(existingCart))
                .when(cartRepository).findByUserIdAndStatus(anyString(), anyString());
        doReturn(Mono.just(updatedCart))
                .when(cartRepository).addItem(anyString(), anyString());

        // Act
        Mono<Cart> result = cartService.addItemToCart(itemId);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(cart -> {
                    assertEquals(existingCartId, cart.getCartId());
                    assertEquals(1, cart.getItems().size());
                    assertEquals(itemId, cart.getItems().getFirst().getGameId());
                    return true;
                })
                .verifyComplete();

        verify(catalogueClient).getGame(itemId);
        verify(cartRepository).findByUserIdAndStatus(USER_DEFAULT_ID, "ACTIVE");
        verify(cartRepository, never()).save(any(Cart.class));
        verify(cartRepository).addItem(existingCartId, itemId);
    }

    private GameResponse createGameResponse(String gameId, boolean available) {
        return GameResponse.builder()
                .id(gameId)
                .available(available)
                .build();
    }

    private Cart createActiveCart(String cartId, String... gameIds) {
        List<CartItem> items = Arrays.stream(gameIds)
                .map(CartItem::new)
                .toList();

        return Cart.builder()
                .cartId(cartId)
                .userId(USER_DEFAULT_ID)
                .items(items)
                .status("ACTIVE")
                .build();
    }
}
