package com.eventhub.backend.service;

import com.eventhub.backend.dto.request.AdminRequests.SaveCategoryRequest;
import com.eventhub.backend.dto.response.AdminResponses.PageResponse;
import com.eventhub.backend.dto.response.CategoryResponse;
import com.eventhub.backend.entity.Category;
import com.eventhub.backend.exception.AdminException;
import com.eventhub.backend.repository.CategoryRepository;
import com.eventhub.backend.repository.EventRepository;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminCategoryService {
    private final CategoryRepository categories;
    private final EventRepository events;

    @Transactional(readOnly = true)
    public PageResponse<CategoryResponse> list(int page, int pageSize) {
        return PageResponse.from(categories.findAll(AdminPagination.page(page, pageSize)).map(this::response));
    }
    public CategoryResponse create(SaveCategoryRequest request) { return save(new Category(), request); }
    public CategoryResponse update(Integer id, SaveCategoryRequest request) { return save(find(id), request); }
    public void delete(Integer id) {
        Category category = find(id);
        if (events.existsByCategoryId(id)) {
            throw new AdminException(HttpStatus.CONFLICT, "CATEGORY_IN_USE", "Danh mục đang được sử dụng");
        }
        // The FK also protects against an event being created concurrently.
        categories.delete(category);
        categories.flush();
    }
    private Category find(Integer id) {
        AdminPagination.validId(id);
        return categories.findById(id).orElseThrow(() ->
                new AdminException(HttpStatus.NOT_FOUND, "CATEGORY_NOT_FOUND", "Không tìm thấy danh mục"));
    }
    private CategoryResponse save(Category category, SaveCategoryRequest request) {
        if (categories.existsNormalizedName(request.name().toLowerCase(Locale.ROOT), category.getId())) {
            throw new AdminException(HttpStatus.CONFLICT, "CATEGORY_NAME_ALREADY_EXISTS", "Tên danh mục đã tồn tại");
        }
        category.setName(request.name());
        category.setDescription(request.description());
        return response(categories.saveAndFlush(category));
    }
    private CategoryResponse response(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getDescription());
    }
}
