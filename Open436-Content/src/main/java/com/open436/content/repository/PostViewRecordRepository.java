package com.open436.content.repository;

import com.open436.content.domain.entity.PostViewRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 帖子浏览记录Repository接口
 */
@Repository
public interface PostViewRecordRepository extends JpaRepository<PostViewRecord, Long> {
    
    /**
     * 查询指定用户在指定时间后对指定帖子的浏览记录
     */
    @Query("SELECT pvr FROM PostViewRecord pvr WHERE pvr.postId = :postId " +
           "AND pvr.userId = :userId AND pvr.viewedAt > :afterTime")
    Optional<PostViewRecord> findRecentViewByUserAndPost(
            @Param("postId") Long postId,
            @Param("userId") Long userId,
            @Param("afterTime") LocalDateTime afterTime
    );
    
    /**
     * 查询指定IP在指定时间后对指定帖子的浏览记录
     */
    @Query("SELECT pvr FROM PostViewRecord pvr WHERE pvr.postId = :postId " +
           "AND pvr.ipAddress = :ipAddress AND pvr.viewedAt > :afterTime")
    Optional<PostViewRecord> findRecentViewByIpAndPost(
            @Param("postId") Long postId,
            @Param("ipAddress") String ipAddress,
            @Param("afterTime") LocalDateTime afterTime
    );
    
    /**
     * 统计指定帖子的浏览记录数量
     */
    long countByPostId(Long postId);
    
    /**
     * 删除指定帖子的浏览记录
     */
    void deleteByPostId(Long postId);
}

