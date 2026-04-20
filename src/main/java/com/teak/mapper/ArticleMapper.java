package com.teak.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teak.model.Article;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文章 Mapper
 */
@Mapper
public interface ArticleMapper extends BaseMapper<Article> {
}
