package com.open436.content.controller;

import com.open436.content.common.PageResult;
import com.open436.content.common.Result;
import com.open436.content.domain.dto.CreatePostDTO;
import com.open436.content.domain.dto.PostQueryDTO;
import com.open436.content.domain.dto.UpdatePostDTO;
import com.open436.content.service.PostService;
import com.open436.content.domain.vo.PostDetailVO;
import com.open436.content.domain.vo.PostListVO;
import com.open436.content.util.AuthUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 帖子控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/content/posts")
@RequiredArgsConstructor
@Tag(name = "帖子管理", description = "帖子的创建、查询、编辑、删除等操作")
public class PostController {
    
    private final PostService postService;
    
    @PostMapping
    @Operation(summary = "发布新帖", description = "用户发布一篇新帖子")
    public Result<Long> createPost(
            @Valid @RequestBody CreatePostDTO createPostDTO,
            @Parameter(hidden = true) @RequestHeader(value = "X-User-Id", required = false) Long userId
    ) {
        // 暂时从请求头获取用户ID，后续需要时可以改为从Sa-Token获取
        if (userId == null) {
            userId = AuthUtil.getCurrentUserIdOrNull();
        }
        if (userId == null) {
            userId = 1L; // 临时默认值，用于测试
        }
        log.info("发布帖子请求 - 用户ID: {}, 标题: {}", userId, createPostDTO.getTitle());
        Long postId = postService.createPost(createPostDTO, userId);
        return Result.success("发布成功", postId);
    }
    
    @GetMapping
    @Operation(summary = "查询帖子列表", description = "分页查询帖子列表，支持板块筛选和排序")
    public Result<PageResult<PostListVO>> listPosts(
            @Parameter(description = "板块ID") @RequestParam(required = false) Long boardId,
            @Parameter(description = "作者用户ID") @RequestParam(required = false) Long authorId,
            @Parameter(description = "排序方式：latest-最新发布，reply-最新回复，hot-热度")
            @RequestParam(defaultValue = "latest") String sortBy,
            @Parameter(description = "是否只查询置顶帖子") @RequestParam(defaultValue = "false") Boolean pinnedOnly,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        log.info("查询帖子列表 - 板块ID: {}, 排序: {}, 页码: {}", boardId, sortBy, page);
        PostQueryDTO queryDTO = new PostQueryDTO();
        queryDTO.setBoardId(boardId);
        queryDTO.setAuthorId(authorId);
        queryDTO.setSortBy(sortBy);
        queryDTO.setPinnedOnly(pinnedOnly);
        queryDTO.setPage(page);
        queryDTO.setPageSize(pageSize);
        
        PageResult<PostListVO> result = postService.listPosts(queryDTO);
        return Result.success(result);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "查看帖子详情", description = "查看指定帖子的完整信息，自动记录浏览量")
    public Result<PostDetailVO> getPostDetail(
            @Parameter(description = "帖子ID") @PathVariable Long id,
            HttpServletRequest request
    ) {
        // 获取访问者ID（可能未登录）
        Long userId = AuthUtil.getCurrentUserIdOrNull();
        log.info("查看帖子详情 - 帖子ID: {}, 用户ID: {}", id, userId);
        // 获取访问者IP
        String ipAddress = getClientIp(request);
        PostDetailVO detail = postService.getPostDetail(id, userId, ipAddress);
        return Result.success(detail);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "编辑帖子", description = "编辑指定帖子的内容，仅作者本人可操作")
    public Result<Void> updatePost(
            @Parameter(description = "帖子ID") @PathVariable Long id,
            @Valid @RequestBody UpdatePostDTO updatePostDTO,
            @Parameter(hidden = true) @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @Parameter(hidden = true) @RequestHeader(value = "X-Is-Admin", required = false, defaultValue = "false") Boolean isAdmin
    ) {
        // 暂时从请求头获取用户ID和管理员标识，后续需要时可以改为从Sa-Token获取
        if (userId == null) {
            userId = AuthUtil.getCurrentUserIdOrNull();
        }
        if (userId == null) {
            userId = 1L; // 临时默认值，用于测试
        }
        if (isAdmin == null) {
            isAdmin = AuthUtil.isAdmin();
        }
        log.info("编辑帖子请求 - 帖子ID: {}, 用户ID: {}, 是否管理员: {}", id, userId, isAdmin);
        postService.updatePost(id, updatePostDTO, userId, isAdmin);
        return Result.success();
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除帖子", description = "删除指定帖子（软删除），仅作者本人可操作")
    public Result<Void> deletePost(
            @Parameter(description = "帖子ID") @PathVariable Long id,
            @Parameter(hidden = true) @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @Parameter(hidden = true) @RequestHeader(value = "X-Is-Admin", required = false, defaultValue = "false") Boolean isAdmin
    ) {
        // 暂时从请求头获取用户ID和管理员标识，后续需要时可以改为从Sa-Token获取
        if (userId == null) {
            userId = AuthUtil.getCurrentUserIdOrNull();
        }
        if (userId == null) {
            userId = 1L; // 临时默认值，用于测试
        }
        if (isAdmin == null) {
            isAdmin = AuthUtil.isAdmin();
        }
        log.info("删除帖子请求 - 帖子ID: {}, 用户ID: {}, 是否管理员: {}", id, userId, isAdmin);
        postService.deletePost(id, userId, isAdmin, null);
        return Result.success();
    }
    
    @GetMapping("/user/{userId}")
    @Operation(summary = "查询用户的帖子列表", description = "查询指定用户发布的所有帖子")
    public Result<PageResult<PostListVO>> getUserPosts(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        log.info("查询用户帖子列表 - 用户ID: {}, 页码: {}", userId, page);
        PageResult<PostListVO> result = postService.getUserPosts(userId, page, pageSize);
        return Result.success(result);
    }
    
    /**
     * 获取客户端真实IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}

