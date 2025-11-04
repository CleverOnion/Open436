package com.open436.content.controller;

import com.open436.content.common.Result;
import com.open436.content.domain.dto.DeletePostDTO;
import com.open436.content.domain.dto.PinPostDTO;
import com.open436.content.service.PostManageService;
import com.open436.content.domain.vo.PostEditHistoryVO;
import com.open436.content.util.AuthUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 帖子管理控制器（管理员功能）
 */
@Slf4j
@RestController
@RequestMapping("/api/content/posts/manage")
@RequiredArgsConstructor
@Tag(name = "帖子管理（管理员）", description = "管理员专用功能：置顶、恢复、硬删除、查看编辑历史等")
public class PostManageController {
    
    private final PostManageService postManageService;
    
    @PutMapping("/{id}/pin")
    @Operation(summary = "置顶帖子", description = "将帖子设置为置顶状态，仅管理员可操作")
    public Result<Void> pinPost(
            @Parameter(description = "帖子ID") @PathVariable Long id,
            @Valid @RequestBody PinPostDTO pinPostDTO,
            @Parameter(hidden = true) @RequestHeader(value = "X-User-Id", required = false) Long userId
    ) {
        // 暂时从请求头获取用户ID，后续需要时可以改为从Sa-Token获取并验证管理员权限
        if (userId == null) {
            userId = AuthUtil.getCurrentUserIdOrNull();
        }
        if (userId == null) {
            userId = 1L; // 临时默认值，用于测试
        }
        log.info("置顶帖子请求 - 帖子ID: {}, 置顶类型: {}, 操作者ID: {}", id, pinPostDTO.getPinType(), userId);
        postManageService.pinPost(id, pinPostDTO.getPinType(), userId);
        return Result.success();
    }
    
    @DeleteMapping("/{id}/pin")
    @Operation(summary = "取消置顶", description = "取消帖子的置顶状态，仅管理员可操作")
    public Result<Void> unpinPost(
            @Parameter(description = "帖子ID") @PathVariable Long id,
            @Parameter(hidden = true) @RequestHeader(value = "X-User-Id", required = false) Long userId
    ) {
        // 暂时从请求头获取用户ID，后续需要时可以改为从Sa-Token获取并验证管理员权限
        if (userId == null) {
            userId = AuthUtil.getCurrentUserIdOrNull();
        }
        if (userId == null) {
            userId = 1L; // 临时默认值，用于测试
        }
        log.info("取消置顶请求 - 帖子ID: {}, 操作者ID: {}", id, userId);
        postManageService.unpinPost(id, userId);
        return Result.success();
    }
    
    @PostMapping("/{id}/restore")
    @Operation(summary = "恢复已删除的帖子", description = "恢复软删除的帖子，仅管理员可操作")
    public Result<Void> restorePost(
            @Parameter(description = "帖子ID") @PathVariable Long id,
            @Parameter(hidden = true) @RequestHeader(value = "X-User-Id", required = false) Long userId
    ) {
        // 暂时从请求头获取用户ID，后续需要时可以改为从Sa-Token获取并验证管理员权限
        if (userId == null) {
            userId = AuthUtil.getCurrentUserIdOrNull();
        }
        if (userId == null) {
            userId = 1L; // 临时默认值，用于测试
        }
        log.info("恢复帖子请求 - 帖子ID: {}, 操作者ID: {}", id, userId);
        postManageService.restorePost(id, userId);
        return Result.success();
    }
    
    @DeleteMapping("/{id}/permanent")
    @Operation(summary = "永久删除帖子", description = "硬删除帖子，数据无法恢复，仅管理员可操作")
    public Result<Void> permanentDeletePost(
            @Parameter(description = "帖子ID") @PathVariable Long id,
            @Valid @RequestBody DeletePostDTO deletePostDTO,
            @Parameter(hidden = true) @RequestHeader(value = "X-User-Id", required = false) Long userId
    ) {
        // 暂时从请求头获取用户ID，后续需要时可以改为从Sa-Token获取并验证管理员权限
        if (userId == null) {
            userId = AuthUtil.getCurrentUserIdOrNull();
        }
        if (userId == null) {
            userId = 1L; // 临时默认值，用于测试
        }
        log.info("永久删除帖子请求 - 帖子ID: {}, 操作者ID: {}, 原因: {}", id, userId, deletePostDTO.getReason());
        postManageService.permanentDeletePost(id, userId, deletePostDTO.getReason());
        return Result.success();
    }
    
    @GetMapping("/{id}/edit-history")
    @Operation(summary = "查看帖子编辑历史", description = "查看帖子的所有编辑记录，仅管理员可操作")
    public Result<List<PostEditHistoryVO>> getEditHistory(
            @Parameter(description = "帖子ID") @PathVariable Long id
    ) {
        // 暂时不验证管理员权限，后续需要时可以添加 @SaCheckRole("admin")
        log.info("查询编辑历史请求 - 帖子ID: {}", id);
        List<PostEditHistoryVO> history = postManageService.getEditHistory(id);
        return Result.success(history);
    }
}

