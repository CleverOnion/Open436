package com.open436.auth.dto;

import com.open436.auth.entity.Role;
import com.open436.auth.entity.UserAuth;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.stream.Collectors;

/**
 * 用户信息响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse {
    
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 角色
     */
    private String role;
    
    /**
     * 账号状态
     */
    private String status;
    
    /**
     * 从 UserAuth 实体创建响应对象
     * @param user 用户实体
     * @param role 角色代码
     * @return UserInfoResponse
     */
    public static UserInfoResponse from(UserAuth user, String role) {
        return UserInfoResponse.builder()
            .id(user.getId())
            .username(user.getUsername())
            .role(role)
            .status(user.getStatus())
            .build();
    }
}

