package com.melodev.storeservice.controller;

import com.melodev.storeservice.client.CatalogueClient;
import com.melodev.storeservice.client.GameResponse;
import com.melodev.storeservice.model.User;
import com.melodev.storeservice.service.LibraryService;
import com.melodev.storeservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/dummy")
@RequiredArgsConstructor
public class DummyController {

    private final CatalogueClient catalogueClient;
    private final UserService userService;
    private final LibraryService libraryService;

    @GetMapping("game/{id}")
    public Mono<GameResponse> getGame(@PathVariable String id) {
        var a = catalogueClient.getGame(id);
        var b = catalogueClient.getGameWithSla(id);
        return a;
    }

    @GetMapping("user/me")
    public Mono<User> me() {
        return userService.me();
    }

    @GetMapping("library/{gameId}/exists")
    public Mono<Boolean> validateGameInLibrary(@PathVariable String gameId) {
        return libraryService.isGameInLibrary(gameId);
    }
}
