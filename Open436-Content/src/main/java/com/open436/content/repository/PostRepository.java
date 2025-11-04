package com.open436.content.repository;

import com.open436.content.domain.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 帖子Repository接口
 */
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * 根据ID查询未删除的帖子
     */
    Optional<Post> findByIdAndIsDeletedFalse(Long id);
    
    /**
     * 查询指定板块的帖子列表（分页）
     */
    Page<Post> findByBoardIdAndIsDeletedFalse(Long boardId, Pageable pageable);
    
    /**
     * 查询所有未删除的帖子列表（分页）
     */
    Page<Post> findByIsDeletedFalse(Pageable pageable);
    
    /**
     * 查询指定作者的帖子列表（分页）
     */
    Page<Post> findByAuthorIdAndIsDeletedFalse(Long authorId, Pageable pageable);
    
    /**
     * 查询置顶帖子列表
     */
    List<Post> findByPinTypeGreaterThanAndIsDeletedFalseOrderByPinTypeDescPinnedAtDesc(Integer pinType);
    
    /**
     * 查询指定板块的置顶帖子
     */
    List<Post> findByBoardIdAndPinTypeGreaterThanAndIsDeletedFalseOrderByPinTypeDescPinnedAtDesc(Long boardId, Integer pinType);
    
    /**
     * 统计指定板块的帖子数量
     */
    long countByBoardIdAndIsDeletedFalse(Long boardId);
    
    /**
     * 统计指定作者的帖子数量
     */
    long countByAuthorIdAndIsDeletedFalse(Long authorId);
    
    /**
     * 增加浏览量
     */
    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :id")
    int incrementViewCount(@Param("id") Long id);
    
    /**
     * 更新回复数
     */
    @Modifying
    @Query("UPDATE Post p SET p.replyCount = :replyCount WHERE p.id = :id")
    int updateReplyCount(@Param("id") Long id, @Param("replyCount") Integer replyCount);
    
    /**
     * 更新点赞数
     */
    @Modifying
    @Query("UPDATE Post p SET p.likeCount = :likeCount WHERE p.id = :id")
    int updateLikeCount(@Param("id") Long id, @Param("likeCount") Integer likeCount);
    
    /**
     * 查询帖子列表（支持复杂条件）
     * 按发布时间倒序，置顶优先
     */
    @Query("SELECT p FROM Post p WHERE " +
           "(:boardId IS NULL OR p.boardId = :boardId) AND " +
           "(:authorId IS NULL OR p.authorId = :authorId) AND " +
           "(:includeDeleted = true OR p.isDeleted = false) AND " +
           "(:pinnedOnly = false OR p.pinType > 0) " +
           "ORDER BY " +
           "CASE WHEN p.pinType > 0 THEN 0 ELSE 1 END, " +
           "p.pinType DESC, " +
           "p.pinnedAt DESC NULLS LAST, " +
           "p.createdAt DESC")
    Page<Post> findPostsWithFilters(
            @Param("boardId") Long boardId,
            @Param("authorId") Long authorId,
            @Param("includeDeleted") Boolean includeDeleted,
            @Param("pinnedOnly") Boolean pinnedOnly,
            Pageable pageable);
    
    /**
     * 查询帖子列表（按最后更新时间排序，用于回复排序）
     * 按更新时间倒序，置顶优先
     */
    @Query("SELECT p FROM Post p WHERE " +
           "(:boardId IS NULL OR p.boardId = :boardId) AND " +
           "(:authorId IS NULL OR p.authorId = :authorId) AND " +
           "(:includeDeleted = true OR p.isDeleted = false) AND " +
           "(:pinnedOnly = false OR p.pinType > 0) " +
           "ORDER BY " +
           "CASE WHEN p.pinType > 0 THEN 0 ELSE 1 END, " +
           "p.pinType DESC, " +
           "p.pinnedAt DESC NULLS LAST, " +
           "COALESCE(p.lastEditedAt, p.createdAt) DESC")
    Page<Post> findPostsSortedByReply(
            @Param("boardId") Long boardId,
            @Param("authorId") Long authorId,
            @Param("includeDeleted") Boolean includeDeleted,
            @Param("pinnedOnly") Boolean pinnedOnly,
            Pageable pageable);
    
    /**
     * 查询帖子列表（按热度排序）
     * 热度计算：likeCount * 10 + replyCount * 5 + viewCount
     * 置顶优先
     */
    @Query("SELECT p FROM Post p WHERE " +
           "(:boardId IS NULL OR p.boardId = :boardId) AND " +
           "(:authorId IS NULL OR p.authorId = :authorId) AND " +
           "(:includeDeleted = true OR p.isDeleted = false) AND " +
           "(:pinnedOnly = false OR p.pinType > 0) " +
           "ORDER BY " +
           "CASE WHEN p.pinType > 0 THEN 0 ELSE 1 END, " +
           "p.pinType DESC, " +
           "p.pinnedAt DESC NULLS LAST, " +
           "(p.likeCount * 10 + p.replyCount * 5 + p.viewCount) DESC, " +
           "p.createdAt DESC")
    Page<Post> findPostsSortedByHot(
            @Param("boardId") Long boardId,
            @Param("authorId") Long authorId,
            @Param("includeDeleted") Boolean includeDeleted,
            @Param("pinnedOnly") Boolean pinnedOnly,
            Pageable pageable);
}

