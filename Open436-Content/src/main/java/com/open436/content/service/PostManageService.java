package com.open436.content.service;

import com.open436.content.domain.vo.PostEditHistoryVO;

import java.util.List;

/**
 * 帖子管理服务接口（管理员功能）
 */
public interface PostManageService {
    
    /**
     * 置顶帖子
     * 
     * @param postId 帖子ID
     * @param pinType 置顶类型：1-板块置顶，2-全局置顶
     * @param operatorId 操作者ID（管理员）
     */
    void pinPost(Long postId, Integer pinType, Long operatorId);
    
    /**
     * 取消置顶
     * 
     * @param postId 帖子ID
     * @param operatorId 操作者ID（管理员）
     */
    void unpinPost(Long postId, Long operatorId);
    
    /**
     * 恢复已删除的帖子
     * 
     * @param postId 帖子ID
     * @param operatorId 操作者ID（管理员）
     */
    void restorePost(Long postId, Long operatorId);
    
    /**
     * 硬删除帖子（永久删除）
     * 
     * @param postId 帖子ID
     * @param operatorId 操作者ID（管理员）
     * @param reason 删除原因
     */
    void permanentDeletePost(Long postId, Long operatorId, String reason);
    
    /**
     * 查看帖子编辑历史
     * 
     * @param postId 帖子ID
     * @return 编辑历史列表
     */
    List<PostEditHistoryVO> getEditHistory(Long postId);
}

