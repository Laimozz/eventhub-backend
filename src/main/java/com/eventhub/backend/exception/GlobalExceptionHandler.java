package com.eventhub.backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import tools.jackson.databind.ObjectMapper;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler {
    private final ObjectMapper objectMapper;

    @ExceptionHandler(AdminException.class)
    ResponseEntity<ApiError> handleAdmin(AdminException exception, HttpServletRequest request) {
        return error(exception.getStatus(), exception.getCode(), exception.getMessage(), request);
    }

    @ExceptionHandler(org.springframework.orm.ObjectOptimisticLockingFailureException.class)
    ResponseEntity<ApiError> handleVersionConflict(HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, "EVENT_VERSION_CONFLICT", "Nội dung đã thay đổi. Vui lòng tải lại.", request);
    }

    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    ResponseEntity<ApiError> handleInvalidParameter(HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Tham số không hợp lệ", request);
    }

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ApiError> handleApi(ResponseStatusException exception, HttpServletRequest request) {
        return ResponseEntity.status(exception.getStatusCode()).body(ApiError.of(exception.getStatusCode().value(),
                "REQUEST_ERROR", exception.getReason(), request.getRequestURI()));
    }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ApiError> handleAuthentication(HttpServletRequest request) {
        return error(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Invalid credentials or session", request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiError> handleAccessDenied(HttpServletRequest request) {
        return error(HttpStatus.FORBIDDEN, "FORBIDDEN", "Access denied", request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(field ->
                errors.putIfAbsent(field.getField(), field.getDefaultMessage()));
        return ResponseEntity.badRequest().body(new ApiError(Instant.now(), 400, "VALIDATION_ERROR",
                "Request validation failed", request.getRequestURI(), errors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiError> handleBadRequest(HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Invalid request", request);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    ResponseEntity<ApiError> handleEmailAlreadyExists(EmailAlreadyExistsException exception, HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS", exception.getMessage(), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiError> handleConflict(DataIntegrityViolationException exception, HttpServletRequest request) {
        if (request.getRequestURI().startsWith("/api/admin/")) {
            for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
                if (cause instanceof org.hibernate.exception.ConstraintViolationException constraint) {
                    String name = constraint.getConstraintName();
                    if ("uk_categories_name_normalized".equals(name))
                        return error(HttpStatus.CONFLICT, "CATEGORY_NAME_ALREADY_EXISTS", "Tên danh mục đã tồn tại", request);
                    if ("uk_users_email_normalized".equals(name) || "uk_users_email".equals(name))
                        return error(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS", "Email đã được sử dụng", request);
                    if ("events_categories_id_fkey".equals(name))
                        return error(HttpStatus.CONFLICT, "CATEGORY_IN_USE", "Danh mục đang được sử dụng", request);
                }
            }
        }
        return error(HttpStatus.CONFLICT, "DATA_CONFLICT", "Data conflicts with an existing record", request);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<ApiError> handleOversizedImage(HttpServletRequest request) {
        return error(HttpStatus.PAYLOAD_TOO_LARGE, "IMAGE_TOO_LARGE", "Images must not exceed 5 MB each or 50 MB per request", request);
    }

    private ResponseEntity<ApiError> error(HttpStatus status, String code, String message,
            HttpServletRequest request) {
        return ResponseEntity.status(status).body(ApiError.of(status.value(), code, message, request.getRequestURI()));
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException {
        write(request, response, 401, "UNAUTHORIZED", "Authentication required or session expired");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException exception) throws IOException {
        write(request, response, 403, "FORBIDDEN", "Access denied or missing CSRF protection header");
    }

    private void write(HttpServletRequest request, HttpServletResponse response, int status,
            String code, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), ApiError.of(status, code, message, request.getRequestURI()));
    }

    public record ApiError(Instant timestamp, int status, String code, String message,
            String path, Map<String, String> errors) {
        static ApiError of(int status, String code, String message, String path) {
            return new ApiError(Instant.now(), status, code, message, path, Map.of());
        }
    }
}
