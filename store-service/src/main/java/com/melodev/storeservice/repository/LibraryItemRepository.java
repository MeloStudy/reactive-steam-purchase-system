package com.melodev.storeservice.repository;

import com.melodev.storeservice.model.LibraryItem;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface LibraryItemRepository extends ReactiveCrudRepository<LibraryItem, String> {
    Mono<Boolean> existsByUserIdAndGameId(String userId, String gameId);
}
