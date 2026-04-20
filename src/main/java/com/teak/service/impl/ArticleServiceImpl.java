package com.teak.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teak.mapper.ArticleMapper;
import com.teak.mapper.WebUserMapper;
import com.teak.model.Article;
import com.teak.model.WebUser;
import com.teak.service.ArticleService;
import com.teak.system.exception.BusinessException;
import com.teak.system.executor.TaskExecuteContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 文章 Service 实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    private final ArticleMapper articleMapper;
    private final WebUserMapper webUserMapper;


    @Override
    public void addArticle(Article article) {

        if (article.getUserId() == null) {
            throw new BusinessException("用户ID不能为空");
        }
        WebUser webUser = webUserMapper.selectById(article.getUserId());
        if (webUser == null) {
            throw new BusinessException("用户不存在");
        }
        articleMapper.insert(article);
    }
}
