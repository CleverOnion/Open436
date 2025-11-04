package com.open436.content.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果类
 * 
 * @param <T> 数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 当前页码
     */
    private Integer page;
    
    /**
     * 每页大小
     */
    private Integer pageSize;
    
    /**
     * 总记录数
     */
    private Long total;
    
    /**
     * 总页数
     */
    private Integer totalPages;
    
    /**
     * 数据列表
     */
    private List<T> records;
    
    /**
     * 是否有下一页
     */
    private Boolean hasNext;
    
    /**
     * 是否有上一页
     */
    private Boolean hasPrevious;
    
    /**
     * 构建分页结果
     */
    public static <T> PageResult<T> build(Integer page, Integer pageSize, Long total, List<T> records) {
        PageResult<T> result = new PageResult<>();
        result.setPage(page);
        result.setPageSize(pageSize);
        result.setTotal(total);
        result.setRecords(records);
        
        // 计算总页数
        int totalPages = (int) Math.ceil((double) total / pageSize);
        result.setTotalPages(totalPages);
        
        // 判断是否有下一页和上一页
        result.setHasNext(page < totalPages);
        result.setHasPrevious(page > 1);
        
        return result;
    }
}

