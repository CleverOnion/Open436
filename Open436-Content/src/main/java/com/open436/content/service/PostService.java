package com.open436.content.service;

import com.open436.content.common.PageResult;
import com.open436.content.domain.dto.CreatePostDTO;
import com.open436.content.domain.dto.PostQueryDTO;
import com.open436.content.domain.dto.UpdatePostDTO;
import com.open436.content.domain.vo.PostDetailVO;
import com.open436.content.domain.vo.PostListVO;

/**
 * 帖子服务接口
 */
public interface PostService {
    
    /**
     * 发布新帖
     * 
     * @param createPostDTO 发布帖子请求
     * @param authorId 作者用户ID（从登录信息获取）
     * @return 帖子ID
     */
    Long createPost(CreatePostDTO createPostDTO, Long authorId);
    
    /**
     * 分页查询帖子列表
     * 
     * @param queryDTO 查询条件
     * @return 帖子列表
     */
    PageResult<PostListVO> listPosts(PostQueryDTO queryDTO);
    
    /**
     * 查看帖子详情
     * 
     * @param id 帖子ID
     * @param viewerId 访问者ID（登录用户），未登录传null
     * @param viewerIp 访问者IP（未登录用户）
     * @param isAdmin 是否为管理员
     * @return 帖子详情
     */
    PostDetailVO getPostDetail(Long id, Long viewerId, String viewerIp, Boolean isAdmin);
    
    /**
     * 编辑帖子
     * 
     * @param id 帖子ID
     * @param updatePostDTO 更新内容
     * @param editorId 编辑者ID（从登录信息获取）
     * @param isAdmin 是否为管理员
     */
    void updatePost(Long id, UpdatePostDTO updatePostDTO, Long editorId, Boolean isAdmin);
    
    /**
     * 删除帖子（软删除）
     * 
     * @param id 帖子ID
     * @param operatorId 操作者ID
     * @param isAdmin 是否为管理员
     * @param reason 删除原因（管理员必填）
     */
    void deletePost(Long id, Long operatorId, Boolean isAdmin, String reason);
    
    /**
     * 查询指定用户的帖子列表
     * 
     * @param userId 用户ID
     * @param page 页码
     * @param pageSize 每页大小
     * @return 帖子列表
     */
    PageResult<PostListVO> getUserPosts(Long userId, Integer page, Integer pageSize);
}

