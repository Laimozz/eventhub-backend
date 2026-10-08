package com.eventhub.backend.controller;

import com.eventhub.backend.dto.request.AdminRequests.SaveCategoryRequest;
import com.eventhub.backend.dto.response.AdminResponses.PageResponse;
import com.eventhub.backend.dto.response.CategoryResponse;
import com.eventhub.backend.service.AdminCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/event-categories")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminCategoryController {
    private final AdminCategoryService categories;
    @GetMapping
    public PageResponse<CategoryResponse> list(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) { return categories.list(page, pageSize); }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(@Valid @RequestBody SaveCategoryRequest request) { return categories.create(request); }
    @PutMapping("/{categoryId}")
    public CategoryResponse update(@PathVariable Integer categoryId, @Valid @RequestBody SaveCategoryRequest request) {
        return categories.update(categoryId, request);
    }
    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer categoryId) { categories.delete(categoryId); }
}
