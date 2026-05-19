package com.melodev.storeservice.repository;

import com.melodev.storeservice.model.Cart;
import com.melodev.storeservice.repository.custom.CartRepositoryCrud;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface CartRepository extends ReactiveMongoRepository<Cart, String>, CartRepositoryCrud {
    Mono<Cart> findByUserIdAndStatus(String userId, String status);
}
