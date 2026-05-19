package com.melodev.storeservice.controller;

import com.melodev.storeservice.client.CatalogueClient;
import com.melodev.storeservice.client.GameResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/dummy")
public class DummyController {

    CatalogueClient catalogueClient;

    public DummyController(CatalogueClient catalogueClient) {
        this.catalogueClient = catalogueClient;
    }

    @GetMapping("/{id}")
    public Mono<GameResponse> getGame(@PathVariable String id) {
        var a = catalogueClient.getGame(id);
        var b = catalogueClient.getGameWithSla(id);
        return a;
    }
}
