package com.eventhub.backend.controller;

import com.eventhub.backend.dto.response.CategoryResponse;
import com.eventhub.backend.service.EventService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final EventService events;

    @GetMapping
    public List<CategoryResponse> getCategories() {
        return events.getCategories();
    }
}
