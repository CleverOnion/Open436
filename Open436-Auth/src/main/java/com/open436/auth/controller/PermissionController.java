package com.open436.auth.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.open436.auth.dto.ApiResponse;
import com.open436.auth.entity.Permission;
import com.open436.auth.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 权限控制器
 * 处理权限查询相关请求
 */
@Slf4j
@RestController
@RequestMapping("/api/auth/permissions")
@RequiredArgsConstructor
public class PermissionController {
    
    private final PermissionService permissionService;
    
    /**
     * 获取当前用户的所有权限
     */
    @GetMapping("/my")
    @SaCheckLogin
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMyPermissions() {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info("获取用户权限: userId={}", userId);
        
        List<Permission> permissions = permissionService.getUserPermissions(userId);
        
        // 按资源分组
        Map<String, List<Map<String, String>>> groupedPermissions = permissions.stream()
            .collect(Collectors.groupingBy(
                Permission::getResource,
                Collectors.mapping(
                    p -> {
                        Map<String, String> map = new HashMap<>();
                        map.put("code", p.getCode());
                        map.put("name", p.getName());
                        map.put("action", p.getAction());
                        return map;
                    },
                    Collectors.toList()
                )
            ));
        
        Map<String, Object> result = new HashMap<>();
        result.put("permissions", groupedPermissions);
        result.put("total", permissions.size());
        
        return ResponseEntity.ok(
            ApiResponse.success("获取成功", result)
        );
    }
    
    /**
     * 检查当前用户是否拥有指定权限
     */
    @GetMapping("/check")
    @SaCheckLogin
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkPermission(
            @RequestParam String code) {
        
        Long userId = StpUtil.getLoginIdAsLong();
        log.info("检查用户权限: userId={}, permissionCode={}", userId, code);
        
        boolean hasPermission = permissionService.hasPermission(userId, code);
        
        Map<String, Object> result = new HashMap<>();
        result.put("permission", code);
        result.put("hasPermission", hasPermission);
        
        return ResponseEntity.ok(
            ApiResponse.success("检查成功", result)
        );
    }
}


