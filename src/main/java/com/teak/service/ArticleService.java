package com.teak.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.teak.model.Article;

/**
 * 文章 Service 接口
 */
public interface ArticleService extends IService<Article> {
    void addArticle(Article article);

}
