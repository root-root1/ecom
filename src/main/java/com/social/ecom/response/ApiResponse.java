package com.social.ecom.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.annotation.Nullable;

@Data
@AllArgsConstructor
public class ApiResponse {
    private String message;
    @Nullable
    private Object data;
}
