package com.open436.content.service.impl;

import com.open436.content.common.exception.BusinessException;
import com.open436.content.common.exception.ResourceNotFoundException;
import com.open436.content.domain.entity.Post;
import com.open436.content.repository.PostEditHistoryRepository;
import com.open436.content.repository.PostRepository;
import com.open436.content.service.PostManageService;
import com.open436.content.domain.vo.PostEditHistoryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 帖子管理服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostManageServiceImpl implements PostManageService {
    
    private final PostRepository postRepository;
    private final PostEditHistoryRepository postEditHistoryRepository;
    
    @Override
    @Transactional
    public void pinPost(Long postId, Integer pinType, Long operatorId) {
        log.info("置顶帖子 - 帖子ID:{}, 置顶类型:{}, 操作者ID:{}", postId, pinType, operatorId);
        
        // 1. 验证置顶类型
        if (pinType == null || (pinType != 1 && pinType != 2)) {
            throw new BusinessException(400, "置顶类型必须为1（板块置顶）或2（全局置顶）");
        }
        
        // 2. 查询帖子（包括已删除的帖子也可以置顶）
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("帖子", postId));
        
        // 3. 检查帖子是否已删除
        if (post.getIsDeleted()) {
            throw new BusinessException(400, "无法置顶已删除的帖子");
        }
        
        // 4. 检查是否已经是相同置顶类型
        if (post.getPinType().equals(pinType)) {
            log.warn("帖子已经是置顶类型 {}", pinType);
            return;
        }
        
        // 5. 更新置顶信息
        post.setPinType(pinType);
        post.setPinnedAt(LocalDateTime.now());
        post.setPinnedBy(operatorId);
        
        // 6. 保存更新
        postRepository.save(post);
        
        log.info("帖子置顶成功 - 帖子ID:{}, 置顶类型:{}", postId, pinType);
    }
    
    @Override
    @Transactional
    public void unpinPost(Long postId, Long operatorId) {
        log.info("取消置顶 - 帖子ID:{}, 操作者ID:{}", postId, operatorId);
        
        // 1. 查询帖子
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("帖子", postId));
        
        // 2. 检查是否已经是非置顶状态
        if (post.getPinType() == 0) {
            log.warn("帖子已经是非置顶状态");
            return;
        }
        
        // 3. 取消置顶
        post.setPinType(0);
        post.setPinnedAt(null);
        post.setPinnedBy(null);
        
        // 4. 保存更新
        postRepository.save(post);
        
        log.info("取消置顶成功 - 帖子ID:{}", postId);
    }
    
    @Override
    @Transactional
    public void restorePost(Long postId, Long operatorId) {
        log.info("恢复帖子 - 帖子ID:{}, 操作者ID:{}", postId, operatorId);
        
        // 1. 查询帖子（包括已删除的）
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("帖子", postId));
        
        // 2. 检查帖子是否已删除
        if (!post.getIsDeleted()) {
            log.warn("帖子未被删除，无需恢复");
            throw new BusinessException(400, "帖子未被删除，无需恢复");
        }
        
        // 3. 恢复帖子
        post.setIsDeleted(false);
        post.setDeleteReason(null);
        post.setDeletedBy(null);
        post.setDeletedAt(null);
        
        // 4. 保存更新
        postRepository.save(post);
        
        log.info("恢复帖子成功 - 帖子ID:{}", postId);
    }
    
    @Override
    @Transactional
    public void permanentDeletePost(Long postId, Long operatorId, String reason) {
        log.info("永久删除帖子 - 帖子ID:{}, 操作者ID:{}, 原因:{}", postId, operatorId, reason);
        
        // 1. 查询帖子
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("帖子", postId));
        
        // 2. 验证删除原因
        if (reason == null || reason.trim().isEmpty()) {
            throw new BusinessException(400, "永久删除帖子必须提供删除原因");
        }
        
        // 3. 记录删除信息（用于日志审计）
        log.warn("永久删除帖子 - ID:{}, 标题:{}, 作者ID:{}, 操作者:{}, 原因:{}", 
                postId, post.getTitle(), post.getAuthorId(), operatorId, reason);
        
        // 4. 执行硬删除
        postRepository.delete(post);
        
        log.info("永久删除帖子成功 - 帖子ID:{}", postId);
    }
    
    @Override
    public List<PostEditHistoryVO> getEditHistory(Long postId) {
        // TODO: 实现查询编辑历史功能
        log.info("TODO: 查询编辑历史 - 帖子ID:{}", postId);
        throw new UnsupportedOperationException("功能待实现：查询编辑历史");
    }
}

