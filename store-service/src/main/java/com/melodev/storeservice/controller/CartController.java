package com.melodev.storeservice.controller;

import com.melodev.storeservice.controller.requests.AddItemRequest;
import com.melodev.storeservice.model.Cart;
import com.melodev.storeservice.service.CartService;
import com.melodev.storeservice.service.GetComputedCartService;
import com.melodev.storeservice.service.dtos.CartResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;
    private final GetComputedCartService getComputedCartService;

    @GetMapping("/history")
    public Flux<Cart> getAllCarts() {
        return cartService.getAllCarts();
    }

    @GetMapping()
    public Mono<CartResponse> getActiveCart() {
        return getComputedCartService.getComputedCart();
    }


    @PostMapping("/items")
    public Mono<Cart> addItemToCart(
            @RequestBody AddItemRequest request
    ) {
        return cartService.addItemToCart(request.getGameId());
    }

}

