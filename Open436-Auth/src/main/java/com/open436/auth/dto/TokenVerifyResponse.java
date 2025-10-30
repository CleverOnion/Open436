package com.open436.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenVerifyResponse {
    private Boolean valid;
    private UserTokenInfo data;
}

