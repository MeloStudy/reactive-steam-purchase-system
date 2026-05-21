package com.melodev.storeservice.service.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melodev.storeservice.model.Cart;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CartResponse {
    @JsonProperty("cart_id")
    private String cartId;

    @JsonProperty("user_id")
    private String userId;

    private List<CartItemResponse> items;
    private double total;

    @JsonProperty("checkout_allowed")
    private Boolean checkoutAllowed;

    private String status;
    private List<String> validations;

    public static CartResponse draft(String userId) {
        return CartResponse.builder()
                .cartId("NOT-DEFINED")
                .userId(userId)
                .items(List.of())
                .status("DRAFT")
                .checkoutAllowed(false)
                .build();
    }

    public static CartResponse defaultEmpty(Cart cart) {
        return CartResponse.builder()
                .cartId(cart.getCartId())
                .userId(cart.getUserId())
                .items(List.of())
                .status("OK")
                .checkoutAllowed(false)
                .build();
    }
}
