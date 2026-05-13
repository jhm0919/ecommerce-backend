package com.team23.customer.global.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private static final String SUCCESS_STATUS = "success";
    private static final String ERROR_STATUS = "error";
    private static final String FAIL_STATUS = "fail";

    private String status;
    private String message;
    private T data;

    //데이터와 함께 성공 반환
    public static <T> ApiResponse<T> createSuccess(T data) {
        return new ApiResponse<>(SUCCESS_STATUS, "성공", data);
    }

    public static <T> ApiResponse<T> createSuccess(String message, T data) {
        return new ApiResponse<>(SUCCESS_STATUS, message, data);
    }

    //데이터 없이 성공 반환
    public static ApiResponse<?> createSuccessWithNoContent() {
        return new ApiResponse<>(SUCCESS_STATUS, "성공", null);
    }

    public static ApiResponse<?> createSuccessWithNoContent(String message) {
        return new ApiResponse<>(SUCCESS_STATUS, message, null);
    }

    //유효성 검증
    public static ApiResponse<?> createFail(BindingResult bindingResult) {
        Map<String, String> errors = new HashMap<>();
        for(ObjectError error : bindingResult.getAllErrors()) {
            if (error instanceof FieldError) {
                errors.put(((FieldError) error).getField(), error.getDefaultMessage());
            } else {
                errors.put(error.getObjectName(), error.getDefaultMessage());
            }
        }

        return new ApiResponse<>(FAIL_STATUS, "입력값 오류", errors);
    }

    //예외
    public static ApiResponse<?> createError(String message) {
        return new ApiResponse<>(ERROR_STATUS, message, null);
    }
}
