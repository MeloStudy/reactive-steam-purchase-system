package com.melodev.storeservice.repository.custom;

import com.melodev.storeservice.model.Cart;
import com.melodev.storeservice.model.CartItem;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Repository
public class CartRepositoryCrudImpl implements CartRepositoryCrud {
    private final ReactiveMongoTemplate reactiveMongoTemplate;

    @Override
    public Mono<Cart> addItem(String cartId, String itemId) {
        Query query = new Query(Criteria.where("_id").is(cartId));
        Update update = new Update()
                .push("items", new CartItem(itemId))
                .set("updated_at", LocalDateTime.now());
        return reactiveMongoTemplate.findAndModify(
                query,
                update,
                new FindAndModifyOptions().returnNew(true),
                Cart.class
        );
    }

    @Override
    public Mono<Cart> removeItem(String cartId, String itemId) {
        Query query = new Query(Criteria.where("_id").is(cartId));
        Update update = new Update()
                .pull("items", new CartItem(itemId))
                .set("updated_at", LocalDateTime.now());
        return reactiveMongoTemplate.findAndModify(
                query,
                update,
                new FindAndModifyOptions().returnNew(true),
                Cart.class
        );
    }
}
