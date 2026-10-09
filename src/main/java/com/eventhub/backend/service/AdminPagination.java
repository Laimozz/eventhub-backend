package com.eventhub.backend.service;

import com.eventhub.backend.exception.AdminException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

final class AdminPagination {
    private AdminPagination() {}
    static PageRequest page(int page, int pageSize) {
        if (page < 0 || pageSize < 1 || pageSize > 100) {
            throw new AdminException(HttpStatus.BAD_REQUEST, "INVALID_PAGE", "Phân trang không hợp lệ");
        }
        return PageRequest.of(page, pageSize, Sort.by("id").descending());
    }
    static void validId(Integer id) {
        if (id == null || id < 1) throw new AdminException(HttpStatus.BAD_REQUEST, "INVALID_ID", "ID không hợp lệ");
    }
}
