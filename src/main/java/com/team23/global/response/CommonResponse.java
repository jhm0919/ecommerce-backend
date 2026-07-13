package com.team23.global.response;

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
public class CommonResponse<T> {
    private static final String SUCCESS_STATUS = "success";
    private static final String ERROR_STATUS = "error";
    private static final String FAIL_STATUS = "fail";

    private String status;
    private String message;
    private T data;

    //데이터와 함께 성공 반환
    public static <T> CommonResponse<T> createSuccess(T data) {
        return new CommonResponse<>(SUCCESS_STATUS, "성공", data);
    }

    public static <T> CommonResponse<T> createSuccess(String message, T data) {
        return new CommonResponse<>(SUCCESS_STATUS, message, data);
    }

    //데이터 없이 성공 반환
    public static CommonResponse<?> createSuccessWithNoContent() {
        return new CommonResponse<>(SUCCESS_STATUS, "성공", null);
    }

    public static CommonResponse<?> createSuccessWithNoContent(String message) {
        return new CommonResponse<>(SUCCESS_STATUS, message, null);
    }

    //유효성 검증
    public static CommonResponse<?> createFail(BindingResult bindingResult) {
        Map<String, String> errors = new HashMap<>();
        for(ObjectError error : bindingResult.getAllErrors()) {
            if (error instanceof FieldError) {
                errors.put(((FieldError) error).getField(), error.getDefaultMessage());
            } else {
                errors.put(error.getObjectName(), error.getDefaultMessage());
            }
        }

        return new CommonResponse<>(FAIL_STATUS, "입력값 오류", errors);
    }

    //예외
    public static CommonResponse<?> createError(String message) {
        return new CommonResponse<>(ERROR_STATUS, message, null);
    }
}
