package com.open436.content.repository;

import com.open436.content.domain.entity.PostEditHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 帖子编辑历史Repository接口
 */
@Repository
public interface PostEditHistoryRepository extends JpaRepository<PostEditHistory, Long> {
    
    /**
     * 查询指定帖子的编辑历史（按时间倒序）
     */
    List<PostEditHistory> findByPostIdOrderByEditedAtDesc(Long postId);
    
    /**
     * 查询指定帖子的编辑历史（按版本号升序）
     */
    List<PostEditHistory> findByPostIdOrderByVersionAsc(Long postId);
    
    /**
     * 统计指定帖子的编辑次数
     */
    long countByPostId(Long postId);
    
    /**
     * 查询指定帖子的最新版本号
     */
    @Query("SELECT MAX(h.version) FROM PostEditHistory h WHERE h.postId = :postId")
    Integer findMaxVersionByPostId(@Param("postId") Long postId);
}

