package com.melodev.storeservice.controller;

import com.melodev.storeservice.client.CatalogueClient;
import com.melodev.storeservice.client.GameResponse;
import com.melodev.storeservice.model.Cart;
import com.melodev.storeservice.model.User;
import com.melodev.storeservice.service.CartService;
import com.melodev.storeservice.service.LibraryService;
import com.melodev.storeservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/dummy")
@RequiredArgsConstructor
public class DummyController {

    private final CatalogueClient catalogueClient;
    private final UserService userService;
    private final LibraryService libraryService;
    private final CartService cartService;

    @GetMapping("game/{id}")
    public Mono<GameResponse> getGameDummy(@PathVariable String id) {
        return catalogueClient.getGame(id);
    }

    @GetMapping("game/{id}/sla")
    public Mono<GameResponse> getGameSlaDummy(@PathVariable String id) {
        return catalogueClient.getGameWithSla(id);
    }


    @GetMapping("user/me")
    public Mono<User> meDummy() {
        return userService.me();
    }

    @GetMapping("library/{gameId}/exists")
    public Mono<Boolean> validateGameInLibraryDummy(@PathVariable String gameId) {
        return libraryService.isGameInLibrary(gameId);
    }

    @GetMapping("cart/all")
    public Flux<Cart> getCartsDummy() {
        return cartService.getAllCarts();
    }

    @GetMapping("cart")
    public Mono<Cart> getActiveCartDummy() {
        return cartService.getActiveCart();
    }

    @GetMapping("cart/add/{gameId}")
    public Mono<Cart> addItemToCartDummy(@PathVariable String gameId) {
        return cartService.addItemToCart(gameId);
    }
}
