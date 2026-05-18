package com.melodev.storeservice.repository;

import com.melodev.storeservice.model.LibraryItem;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LibraryItemRepository extends ReactiveCrudRepository<LibraryItem, String> {
}
