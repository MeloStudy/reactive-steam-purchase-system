package com.melodev.storeservice.repository.custom;

import com.melodev.storeservice.model.Cart;
import reactor.core.publisher.Mono;

public interface CartRepositoryCrud {
    Mono<Cart> addItem(String cartId, String itemId);

    Mono<Cart> removeItem(String cartId, String itemId);
}
