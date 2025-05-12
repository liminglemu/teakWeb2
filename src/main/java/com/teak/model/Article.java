package com.teak.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.teak.system.annotation.SnowflakeAlgorithm;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * Created with: IntelliJ IDEA
 *
 * @Author: li zheng
 * @Date: 2025/2/27 00:01
 * @Project: teakWeb
 * @File: Article.java
 * @Description:
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value = "article")
@Data
public class Article extends BaseModel implements Serializable {
    @SnowflakeAlgorithm
    private Long id;

    /**
     * 文章分标题
     */
    private String title;

    /**
     * 文章分类Id
     */
    private Long cateId;

    @TableField(exist = false)
    private String cateName;

    private Long userId;

    @TableField(exist = false)
    private String userName;
}
