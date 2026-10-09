package com.eventhub.backend.repository;

import com.eventhub.backend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    @org.springframework.data.jpa.repository.Query(
            "select count(c) > 0 from Category c where lower(trim(c.name)) = :name and (:excludedId is null or c.id <> :excludedId)")
    boolean existsNormalizedName(String name, Integer excludedId);
}
