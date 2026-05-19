package com.melodev.storeservice.controller.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class AddItemRequest {
    @JsonProperty("game_id")
    private String gameId;
}
