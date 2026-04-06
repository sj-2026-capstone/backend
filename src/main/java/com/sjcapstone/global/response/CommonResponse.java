package com.sjcapstone.global.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommonResponse<T> {
    private final boolean success;
    private final String message;
    private final T data;

    public static <T> CommonResponse<T> ok(String message, T data) {
        return new CommonResponse<>(true, message, data);
    }

    public static CommonResponse<Void> ok(String message) {
        return new CommonResponse<>(true, message, null);
    }

    public static CommonResponse<Void> fail(String message) {
        return new CommonResponse<>(false, message, null);
    }
}
