package com.melodev.storeservice.service.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartItemResponse {
    private String id;
    @JsonProperty("original_price")
    private double originalPrice;
    private double discount;
    @JsonProperty("final_price")
    private double finalPrice;
    private String status;
    private String disclaimer;
}
