package com.melodev.storeservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

import static com.melodev.storeservice.config.ContextUser.USER_DEFAULT_ID;

@Data
@Builder
@AllArgsConstructor
@Document(collection = "cart")
public class Cart {
    @Id
    private String cartId;
    @Field("user_id")
    private String userId;
    private List<CartItem> items;
    @Field("updated_at")
    private LocalDateTime updatedAt;
    private String status;

    public boolean isInCart(String gameId) {
        return items.stream().anyMatch(item -> item.getGameId().equals(gameId));
    }

    public static Cart perDefault() {
        return Cart.builder()
                .userId(USER_DEFAULT_ID)
                .items(List.of())
                .updatedAt(LocalDateTime.now())
                .status("ACTIVE")
                .build();
    }

    public static Cart draft() {
        return Cart.builder()
                .cartId("NOT-DEFINED")
                .userId(USER_DEFAULT_ID)
                .items(List.of())
                .updatedAt(LocalDateTime.now())
                .status("DRAFT")
                .build();
    }
}