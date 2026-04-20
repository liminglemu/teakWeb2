package com.teak.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teak.model.Article;
import com.teak.service.ArticleService;
import com.teak.system.result.GlobalResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

/**
 * 文章控制器
 */
@RestController
@RequestMapping("/api/article")
@RequiredArgsConstructor
@Tag(name = "文章", description = "文章管理相关接口")
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping("/page")
    @Operation(summary = "分页查询文章", description = "分页查询文章列表，支持标题模糊搜索")
    public GlobalResult page(
            @Parameter(description = "当前页，默认1") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页条数，默认10") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "用户ID筛选") @RequestParam(required = false) Long userId,
            @Parameter(description = "标题（模糊查询）") @RequestParam(required = false) String title) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<Article>()
                .like(title != null && !title.isBlank(), Article::getTitle, title)
                .eq(userId != null, Article::getUserId, userId)
                .orderByDesc(Article::getCreateTime);
        Page<Article> page = articleService.page(Page.of(current, size), wrapper);
        return GlobalResult.success(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询单篇文章", description = "根据主键ID获取文章详情")
    public GlobalResult getById(@PathVariable Long id) {
        Article article = articleService.getById(id);
        if (article == null) {
            return GlobalResult.error("文章不存在: ID=" + id);
        }
        return GlobalResult.success(article);
    }

    @PostMapping
    @Operation(summary = "新增文章", description = "添加新文章")
    public GlobalResult add(@RequestBody Article article) {
        articleService.addArticle(article);
        return GlobalResult.success(null, "文章新增成功");
    }

    @PutMapping
    @Operation(summary = "更新文章", description = "根据ID更新文章信息")
    public GlobalResult update(@RequestBody Article article) {
        if (article.getId() == null) {
            return GlobalResult.error("更新时ID不能为空");
        }
        boolean ok = articleService.updateById(article);
        return ok ? GlobalResult.success(null, "文章更新成功") : GlobalResult.error("文章不存在或更新失败");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除文章", description = "逻辑删除指定文章")
    public GlobalResult delete(@PathVariable Long id) {
        boolean ok = articleService.removeById(id);
        return ok ? GlobalResult.success(null, "文章删除成功") : GlobalResult.error("文章不存在或删除失败");
    }
}
