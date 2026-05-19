package com.melodev.storeservice.service;

import com.melodev.storeservice.repository.LibraryItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static com.melodev.storeservice.config.ContextUser.USER_DEFAULT_ID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LibraryService {
    private final LibraryItemRepository libraryItemRepository;
    private final DatabaseClient databaseClient;

    public Mono<Boolean> isGameInLibrary(String gameId) {
        log.info("Looking for game {} in library of user {}", gameId, USER_DEFAULT_ID);

        // Option A: repository.existsByUserIdAndGameId

        // Option B:
        return databaseClient.sql("SELECT EXISTS(SELECT 1 FROM library_items WHERE user_id = :userId AND game_id = :gameId)")
                .bind("userId", USER_DEFAULT_ID)
                .bind("gameId", gameId)
                .map((row, metadata) -> {
                    Boolean result = row.get(0, Boolean.class);
                    return result != null && result;
                })
                .one()
                .defaultIfEmpty(false);
    }
}
