package com.melodev.storeservice.repository;

import com.melodev.storeservice.model.Cart;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface CartRepository extends ReactiveMongoRepository<Cart, String> {

}
