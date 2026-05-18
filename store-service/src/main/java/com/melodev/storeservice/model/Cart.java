package com.melodev.storeservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

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
}