package com.open436.content.service.impl;

import com.open436.content.repository.PostEditHistoryRepository;
import com.open436.content.repository.PostRepository;
import com.open436.content.service.PostManageService;
import com.open436.content.domain.vo.PostEditHistoryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        // TODO: 实现置顶帖子功能
        log.info("TODO: 置顶帖子 - 帖子ID:{}, 置顶类型:{}, 操作者ID:{}", postId, pinType, operatorId);
        throw new UnsupportedOperationException("功能待实现：置顶帖子");
    }
    
    @Override
    @Transactional
    public void unpinPost(Long postId, Long operatorId) {
        // TODO: 实现取消置顶功能
        log.info("TODO: 取消置顶 - 帖子ID:{}, 操作者ID:{}", postId, operatorId);
        throw new UnsupportedOperationException("功能待实现：取消置顶");
    }
    
    @Override
    @Transactional
    public void restorePost(Long postId, Long operatorId) {
        // TODO: 实现恢复帖子功能
        log.info("TODO: 恢复帖子 - 帖子ID:{}, 操作者ID:{}", postId, operatorId);
        throw new UnsupportedOperationException("功能待实现：恢复帖子");
    }
    
    @Override
    @Transactional
    public void permanentDeletePost(Long postId, Long operatorId, String reason) {
        // TODO: 实现硬删除帖子功能
        log.info("TODO: 硬删除帖子 - 帖子ID:{}, 操作者ID:{}, 原因:{}", postId, operatorId, reason);
        throw new UnsupportedOperationException("功能待实现：硬删除帖子");
    }
    
    @Override
    public List<PostEditHistoryVO> getEditHistory(Long postId) {
        // TODO: 实现查询编辑历史功能
        log.info("TODO: 查询编辑历史 - 帖子ID:{}", postId);
        throw new UnsupportedOperationException("功能待实现：查询编辑历史");
    }
}

