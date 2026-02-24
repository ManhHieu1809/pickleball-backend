package com.pickleball.presentation.advices;

import com.pickleball.presentation.helpers.ResponseHelper;
import com.pickleball.presentation.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("Lỗi xác thực: {}", errors);
        return ResponseHelper.badRequest("Xác thực không thành công");
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleBindException(BindException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });

        log.warn("Lỗi liên kết: {}", errors);
        return ResponseHelper.badRequest("Tham số yêu cầu không hợp lệ");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());
        return ResponseHelper.badRequest(ex.getMessage());
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<Object>> handleOptimisticLockingFailure(ObjectOptimisticLockingFailureException ex) {
        log.error("Optimistic locking failure - Entity: {}, Identifier: {}",
                ex.getPersistentClassName(), ex.getIdentifier(), ex);
        return ResponseHelper.conflict("The record has been modified by another user. Please try again.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.error("Data integrity violation - Full details:", ex);

        // Log root cause để debug
        Throwable rootCause = ex.getRootCause();
        if (rootCause != null) {
            log.error("Root cause: {}", rootCause.getMessage());
        }

        String message = "Data integrity constraint violated";
        String detailMessage = ex.getMessage();

        if (detailMessage != null) {
            if (detailMessage.contains("email")) {
                message = "Email already exists";
            } else if (detailMessage.contains("phone")) {
                message = "Phone number already exists";
            } else if (detailMessage.contains("Duplicate entry")) {
                message = "Duplicate entry - Record already exists";
            } else if (detailMessage.contains("foreign key constraint")) {
                // Extract constraint name
                message = "Foreign key constraint violation";
                if (rootCause != null) {
                    message = message + ": " + rootCause.getMessage();
                }
            }
        }

        return ResponseHelper.conflict(message);
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDenied(org.springframework.security.access.AccessDeniedException ex) {
        log.warn("Quyền truy cập bị từ chối: {}", ex.getMessage());
        return ResponseHelper.forbidden("Quyền truy cập bị từ chối. Bạn không có quyền truy cập vào tài nguyên này.");
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(RuntimeException ex) {
        log.error("Lỗi thời gian chạy: ", ex);
        // Trả về message cụ thể thay vì message chung chung
        String errorMessage = ex.getMessage() != null ? ex.getMessage() : "Đã xảy ra lỗi không mong muốn";
        return ResponseHelper.internalError(errorMessage);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex) {
        log.error("Lỗi không mong muốn: ", ex);
        // Trả về message cụ thể thay vì message chung chung
        String errorMessage = ex.getMessage() != null ? ex.getMessage() : "Đã xảy ra lỗi không mong muốn";
        return ResponseHelper.internalError(errorMessage);
    }


}
