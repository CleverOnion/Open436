package com.open436.content.service.impl;

import com.open436.content.common.PageResult;
import com.open436.content.domain.dto.CreatePostDTO;
import com.open436.content.domain.dto.PostQueryDTO;
import com.open436.content.domain.dto.UpdatePostDTO;
import com.open436.content.domain.entity.Post;
import com.open436.content.domain.entity.PostViewRecord;
import com.open436.content.repository.PostEditHistoryRepository;
import com.open436.content.repository.PostRepository;
import com.open436.content.repository.PostViewRecordRepository;
import com.open436.content.service.PostService;
import com.open436.content.domain.vo.PostDetailVO;
import com.open436.content.domain.vo.PostListVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 帖子服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
    
    private final PostRepository postRepository;
    private final PostEditHistoryRepository postEditHistoryRepository;
    private final PostViewRecordRepository postViewRecordRepository;
    
    @Override
    @Transactional
    public Long createPost(CreatePostDTO createPostDTO, Long authorId) {
        log.info("创建帖子 - 标题:{}, 作者ID:{}, 板块ID:{}", 
                 createPostDTO.getTitle(), authorId, createPostDTO.getBoardId());
        
        // 创建Post实体
        Post post = new Post();
        post.setTitle(createPostDTO.getTitle());
        post.setContent(createPostDTO.getContent());
        post.setBoardId(createPostDTO.getBoardId());
        post.setAuthorId(authorId);
        
        // 设置默认值
        post.setIsDeleted(false);
        post.setPinType(0);
        post.setViewCount(0L);
        post.setReplyCount(0);
        post.setLikeCount(0);
        post.setEditCount(0);
        
        // 保存到数据库（createdAt和updatedAt由@PrePersist自动设置）
        Post savedPost = postRepository.save(post);
        
        log.info("帖子创建成功 - 帖子ID:{}", savedPost.getId());
        
        // TODO: 调用用户服务API更新用户发帖数
        // 需要调用 M2 用户管理服务的内部API: POST /internal/users/{authorId}/statistics/increment
        // 请求体: {"field": "posts_count", "value": 1}

        //TODO: 调用文件服务API上传帖子图片
        // 需要调用 M7 文件服务API: POST /files/upload
        
        return savedPost.getId();
    }
    
    @Override
    public PageResult<PostListVO> listPosts(PostQueryDTO queryDTO) {
        log.info("查询帖子列表 - 查询条件:{}", queryDTO);

        // 参数校验和默认值设置
        Integer page = queryDTO.getPage() != null && queryDTO.getPage() > 0 ? queryDTO.getPage() : 1;
        Integer pageSize = queryDTO.getPageSize() != null && queryDTO.getPageSize() > 0
                ? Math.min(queryDTO.getPageSize(), 50) : 20; // 最大50条
        Boolean includeDeleted = queryDTO.getIncludeDeleted() != null ? queryDTO.getIncludeDeleted() : false;
        Boolean pinnedOnly = queryDTO.getPinnedOnly() != null ? queryDTO.getPinnedOnly() : false;
        String sortBy = queryDTO.getSortBy() != null ? queryDTO.getSortBy() : "latest";

        // 创建分页对象（Spring Data JPA页码从0开始）
        Pageable pageable = PageRequest.of(page - 1, pageSize);

        // 根据排序方式选择不同的查询方法
        Page<Post> postPage;
        switch (sortBy.toLowerCase()) {
            case "reply":
                postPage = postRepository.findPostsSortedByReply(
                        queryDTO.getBoardId(),
                        queryDTO.getAuthorId(),
                        includeDeleted,
                        pinnedOnly,
                        pageable);
                break;
            case "hot":
                postPage = postRepository.findPostsSortedByHot(
                        queryDTO.getBoardId(),
                        queryDTO.getAuthorId(),
                        includeDeleted,
                        pinnedOnly,
                        pageable);
                break;
            case "latest":
            default:
                postPage = postRepository.findPostsWithFilters(
                        queryDTO.getBoardId(),
                        queryDTO.getAuthorId(),
                        includeDeleted,
                        pinnedOnly,
                        pageable);
                break;
        }

        // 转换为VO列表
        List<PostListVO> voList = postPage.getContent().stream()
                .map(this::convertToPostListVO)
                .collect(Collectors.toList());

        // 构建分页结果
        return PageResult.build(page, pageSize, postPage.getTotalElements(), voList);
    }
    
    @Override
    @Transactional
    public PostDetailVO getPostDetail(Long id, Long viewerId, String viewerIp, Boolean isAdmin) {
        log.info("查看帖子详情 - 帖子ID:{}, 访问者ID:{}, IP:{}, 是否管理员:{}", id, viewerId, viewerIp, isAdmin);
        
        // 1. 查询帖子（包括已删除的）
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new com.open436.content.common.exception.ResourceNotFoundException("帖子", id));
        
        // 2. 权限检查：已删除的帖子仅管理员可见
        if (post.getIsDeleted() && !Boolean.TRUE.equals(isAdmin)) {
            log.warn("非管理员用户尝试访问已删除的帖子 - 帖子ID:{}, 访问者ID:{}", id, viewerId);
            throw new com.open436.content.common.exception.ResourceNotFoundException("帖子", id);
        }
        
        // 3. 记录浏览量（需要满足业务规则）
        recordPostView(post, viewerId, viewerIp);
        
        // 4. 转换为VO
        PostDetailVO detailVO = convertToPostDetailVO(post);
        
        log.info("帖子详情查询成功 - 帖子ID:{}, 浏览量:{}", id, post.getViewCount());
        return detailVO;
    }
    
    /**
     * 记录帖子浏览量
     * 业务规则：
     * 1. 每次访问浏览量+1
     * 2. 同一用户10分钟内多次访问不重复计数
     * 3. 作者本人访问不计入浏览量
     */
    private void recordPostView(Post post, Long viewerId, String viewerIp) {
        // 作者本人访问不计入浏览量
        if (viewerId != null && viewerId.equals(post.getAuthorId())) {
            log.debug("作者本人访问，不计入浏览量 - 帖子ID:{}, 作者ID:{}", post.getId(), viewerId);
            return;
        }
        
        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);
        Optional<PostViewRecord> recentView;
        
        // 检查10分钟内是否访问过
        if (viewerId != null) {
            recentView = postViewRecordRepository.findRecentViewByUserAndPost(
                    post.getId(), viewerId, tenMinutesAgo);
        } else if (viewerIp != null && !viewerIp.isEmpty()) {
            recentView = postViewRecordRepository.findRecentViewByIpAndPost(
                    post.getId(), viewerIp, tenMinutesAgo);
        } else {
            log.warn("无法记录浏览量：既没有用户ID也没有IP - 帖子ID:{}", post.getId());
            return;
        }
        
        // 如果10分钟内已访问过，不重复计数
        if (recentView.isPresent()) {
            log.debug("10分钟内重复访问，不计入浏览量 - 帖子ID:{}, 访问者ID:{}, IP:{}", 
                     post.getId(), viewerId, viewerIp);
            return;
        }
        
        // 创建浏览记录并更新浏览量
        PostViewRecord viewRecord = new PostViewRecord();
        viewRecord.setPostId(post.getId());
        viewRecord.setUserId(viewerId);
        viewRecord.setIpAddress(viewerId == null ? viewerIp : null);
        postViewRecordRepository.save(viewRecord);
        
        post.setViewCount(post.getViewCount() + 1);
        postRepository.save(post);
        
        log.debug("记录浏览量 - 帖子ID:{}, 访问者ID:{}, IP:{}", post.getId(), viewerId, viewerIp);
    }
    
    /**
     * 计算帖子是否已编辑
     */
    private Boolean calculateIsEdited(Post post) {
        return post.getLastEditedAt() != null && 
               post.getLastEditedAt().isAfter(post.getCreatedAt());
    }
    
    /**
     * 将Post实体转换为PostDetailVO
     */
    private PostDetailVO convertToPostDetailVO(Post post) {
        return PostDetailVO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .authorId(post.getAuthorId())
                .authorName(null) // TODO: 调用M2用户服务获取用户信息
                .authorAvatar(null) // TODO: 调用M2用户服务获取用户头像
                .boardId(post.getBoardId())
                .boardName(null) // TODO: 调用M5板块服务获取板块名称
                .pinType(post.getPinType())
                .isDeleted(post.getIsDeleted())
                .deleteReason(post.getDeleteReason())
                .viewCount(post.getViewCount())
                .replyCount(post.getReplyCount())
                .likeCount(post.getLikeCount())
                .editCount(post.getEditCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .lastEditedAt(post.getLastEditedAt())
                .lastEditedBy(post.getLastEditedBy())
                .isEdited(calculateIsEdited(post))
                .build();
    }
    
    @Override
    @Transactional
    public void updatePost(Long id, UpdatePostDTO updatePostDTO, Long editorId, Boolean isAdmin) {
        // TODO: 实现编辑帖子功能
        log.info("TODO: 编辑帖子 - 帖子ID:{}, 编辑者ID:{}, 是否管理员:{}", id, editorId, isAdmin);
        throw new UnsupportedOperationException("功能待实现：编辑帖子");
    }
    
    @Override
    @Transactional
    public void deletePost(Long id, Long operatorId, Boolean isAdmin, String reason) {
        // TODO: 实现删除帖子功能
        log.info("TODO: 删除帖子 - 帖子ID:{}, 操作者ID:{}, 是否管理员:{}, 原因:{}", id, operatorId, isAdmin, reason);
        throw new UnsupportedOperationException("功能待实现：删除帖子");
    }
    
    @Override
    public PageResult<PostListVO> getUserPosts(Long userId, Integer page, Integer pageSize) {
        log.info("查询用户帖子列表 - 用户ID:{}, 页码:{}, 每页大小:{}", userId, page, pageSize);
        
        // 参数校验和默认值设置
        Integer pageNum = page != null && page > 0 ? page : 1;
        Integer size = pageSize != null && pageSize > 0 ? Math.min(pageSize, 50) : 20;
        
        // 创建分页对象
        Pageable pageable = PageRequest.of(pageNum - 1, size);
        
        // 查询用户帖子（按发布时间倒序）
        Page<Post> postPage = postRepository.findByAuthorIdAndIsDeletedFalse(userId, pageable);
        
        // 转换为VO列表
        List<PostListVO> voList = postPage.getContent().stream()
                .map(this::convertToPostListVO)
                .collect(Collectors.toList());
        
        // 构建分页结果
        return PageResult.build(pageNum, size, postPage.getTotalElements(), voList);
    }
    
    /**
     * 将Post实体转换为PostListVO
     */
    private PostListVO convertToPostListVO(Post post) {
        return PostListVO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .contentPreview(extractContentPreview(post.getContent()))
                .authorId(post.getAuthorId())
                .authorName(null) // TODO: 调用M2用户服务获取用户信息
                .authorAvatar(null) // TODO: 调用M2用户服务获取用户头像
                .boardId(post.getBoardId())
                .boardName(null) // TODO: 调用M5板块服务获取板块名称
                .pinType(post.getPinType())
                .viewCount(post.getViewCount())
                .replyCount(post.getReplyCount())
                .likeCount(post.getLikeCount())
                .createdAt(post.getCreatedAt())
                .lastEditedAt(post.getLastEditedAt())
                .isEdited(calculateIsEdited(post))
                .build();
    }
    
    /**
     * 提取内容预览（前200字，去除HTML标签和格式化标记）
     */
    private String extractContentPreview(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        
        // 去除HTML标签（简单处理，实际应该使用更完善的HTML解析库）
        String plainText = content
                .replaceAll("<[^>]+>", "") // 去除HTML标签
                .replaceAll("\\s+", " ") // 合并多个空白字符
                .trim();
        
        // 提取前200个字符
        if (plainText.length() <= 200) {
            return plainText;
        } else {
            return plainText.substring(0, 200) + "...";
        }
    }
}

