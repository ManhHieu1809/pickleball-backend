package com.pickleball.presentation.helpers;

import com.pickleball.presentation.responses.ApiResponse;
import com.pickleball.presentation.responses.PaginatedResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;


public class ResponseHelper {

    // Success responses
    public static <T> ResponseEntity<ApiResponse<T>> ok(T data) {
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    public static <T> ResponseEntity<ApiResponse<T>> ok(T data, String message) {
        return ResponseEntity.ok(ApiResponse.success(data, message));
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(T data) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(data, "Đã tạo tài nguyên thành công"));
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(T data, String message) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(data, message));
    }

    // Paginated responses
    public static <T> ResponseEntity<ApiResponse<PaginatedResponse<T>>> ok(Page<T> page) {
        PaginatedResponse<T> paginatedData = PaginatedResponse.of(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
        );
        return ResponseEntity.ok(ApiResponse.success(paginatedData));
    }

    public static <T> ResponseEntity<ApiResponse<PaginatedResponse<T>>> ok(
            List<T> content, int page, int size, long totalElements) {
        PaginatedResponse<T> paginatedData = PaginatedResponse.of(content, page, size, totalElements);
        return ResponseEntity.ok(ApiResponse.success(paginatedData));
    }

    // Error responses
    public static <T> ResponseEntity<ApiResponse<T>> badRequest(String message) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(message, "BAD_REQUEST"));
    }

    public static <T> ResponseEntity<ApiResponse<T>> notFound(String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(message, "NOT_FOUND"));
    }

    public static <T> ResponseEntity<ApiResponse<T>> unauthorized(String message) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(message, "UNAUTHORIZED"));
    }

    public static <T> ResponseEntity<ApiResponse<T>> forbidden(String message) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(message, "FORBIDDEN"));
    }

    public static <T> ResponseEntity<ApiResponse<T>> conflict(String message) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(message, "CONFLICT"));
    }

    public static <T> ResponseEntity<ApiResponse<T>> internalError(String message) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message, "INTERNAL_SERVER_ERROR"));
    }

    public static <T> ResponseEntity<ApiResponse<T>> error(HttpStatus status, String message, String errorCode) {
        return ResponseEntity.status(status)
                .body(ApiResponse.error(message, errorCode));
    }
}
