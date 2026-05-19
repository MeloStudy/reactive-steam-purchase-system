package com.melodev.storeservice.config;

import com.melodev.storeservice.model.Cart;
import com.melodev.storeservice.model.CartItem;
import com.melodev.storeservice.model.LibraryItem;
import com.melodev.storeservice.model.User;
import com.melodev.storeservice.repository.CartRepository;
import com.melodev.storeservice.repository.LibraryItemRepository;
import com.melodev.storeservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

import static com.melodev.storeservice.config.ContextUser.USER_DEFAULT_ID;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class SqlDataInitializer implements CommandLineRunner {
    private final DatabaseClient databaseClient;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final ReactiveMongoTemplate mongoTemplate;
    private final LibraryItemRepository libraryItemRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;

    @Override
    public void run(String... args) {
        log.info("Refreshing data for the lab...");
        createTableUsers()
                .then(createTableLibrary())
                .then(seedMainUser())
                .then(seedLibrary())
                .then(createCartCollection())
                .then(seedCarts())
                .doOnSuccess(v -> log.info("✅ Data initialization completed"))
                .doOnError(error -> log.error("❌ Error data initialization: ", error))
                .block();
    }


    private Mono<Void> createTableUsers() {
        return databaseClient.sql(
                "CREATE TABLE IF NOT EXISTS `users` (user_id VARCHAR(100) PRIMARY KEY, name VARCHAR(100))"
        ).then();
    }

    private Mono<Void> createTableLibrary() {
        return databaseClient.sql(
                "CREATE TABLE IF NOT EXISTS library_items (item_id BIGINT AUTO_INCREMENT PRIMARY KEY, user_id VARCHAR(100) , game_id VARCHAR(100), purchase_date TIMESTAMP)"
        ).then();
    }

    private Mono<Void> createCartCollection() {
        return mongoTemplate.collectionExists("cart")
                .flatMap(exists -> {
                    if (!exists) {
                        return mongoTemplate.createCollection("cart");
                    }
                    return Mono.empty();
                })
                .then();
    }


    private Mono<Void> seedMainUser() {
        // no active cart
        User defaultUser = User.builder().userId(USER_DEFAULT_ID).name("MeloDev").build();

        return userRepository.deleteAll()
                .thenMany(r2dbcEntityTemplate.insert(defaultUser))
                .doOnNext(user -> log.info("Seeded User: {} with ID {}", user.getName(), user.getUserId()))
                .doOnComplete(() -> log.info("Data initialization for H2 User completed"))
                .doOnError(error -> log.error("Error seeding H2 User: {}", error.getMessage()))
                .then();
    }

    private Mono<Void> seedLibrary() {
        List<LibraryItem> games = List.of(
                LibraryItem.builder().userId(USER_DEFAULT_ID).gameId("GAME-005").purchaseDate(LocalDateTime.now()).build(),
                LibraryItem.builder().userId(USER_DEFAULT_ID).gameId("GAME-007").purchaseDate(LocalDateTime.now()).build()
        );

        return libraryItemRepository.deleteAll()
                .thenMany(libraryItemRepository.saveAll(games))
                .doOnNext(game -> log.info("Seeded Library Item: {} with ID {}", game.getGameId(), game.getItemId()))
                .doOnComplete(() -> log.info("Data initialization for H2 Library completed"))
                .doOnError(error -> log.error("Error seeding H2 Library: {}", error.getMessage()))
                .then();
    }

    private Mono<Void> seedCarts() {
        List<Cart> carts = List.of(
                Cart.builder().cartId("CART-001")
                        .userId(USER_DEFAULT_ID)
                        .items(
                                List.of(CartItem.builder().gameId("GAME-001").build())
                        ).updatedAt(LocalDateTime.now())
                        .status("CLOSED")
                        .build()
        );

        return cartRepository.deleteAll()
                .thenMany(cartRepository.saveAll(carts))
                .doOnNext(cart -> log.info("Seeded cart: {}", cart.getCartId()))
                .doOnComplete(() -> log.info("Data initialization for MongoDB Cart Collection completed"))
                .doOnError(error -> log.error("Error seeding MongoDB Cart: {}", error.getMessage()))
                .then();
    }


}