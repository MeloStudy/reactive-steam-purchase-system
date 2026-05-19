package com.melodev.storeservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("library_items")
public class LibraryItem {
    @Id
    private Long itemId;
    private String userId;
    private String gameId;
    private LocalDateTime purchaseDate;
}