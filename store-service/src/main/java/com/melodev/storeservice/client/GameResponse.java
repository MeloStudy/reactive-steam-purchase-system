package com.melodev.storeservice.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameResponse {
    private String id;
    private String name;
    private List<String> categories;
    private double price;
    private boolean available;
    private double discountPercentage;
    private boolean activeDiscount;

    public boolean isDummy() {
        return id == null;
    }

    public static GameResponse dummy() {
        return GameResponse.builder().build();
    }
}
