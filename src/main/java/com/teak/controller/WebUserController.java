package com.teak.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teak.model.WebUser;
import com.teak.service.WebUserService;
import com.teak.system.result.GlobalResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

/**
 * Web用户控制器
 */
@RestController
@RequestMapping("/api/webUser")
@RequiredArgsConstructor
@Tag(name = "Web用户", description = "Web用户管理相关接口")
public class WebUserController {

    private final WebUserService webUserService;

    @GetMapping("/page")
    @Operation(summary = "分页查询用户", description = "分页查询Web用户列表")
    public GlobalResult page(
            @Parameter(description = "当前页，默认1") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页条数，默认10") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "用户名（模糊查询）") @RequestParam(required = false) String userName) {
        LambdaQueryWrapper<WebUser> wrapper = new LambdaQueryWrapper<WebUser>()
                .like(userName != null && !userName.isBlank(), WebUser::getUserName, userName)
                .orderByDesc(WebUser::getCreateTime);
        Page<WebUser> page = webUserService.page(Page.of(current, size), wrapper);
        return GlobalResult.success(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询单个用户", description = "根据主键ID获取用户详情")
    public GlobalResult getById(@PathVariable Long id) {
        WebUser user = webUserService.getById(id);
        if (user == null) {
            return GlobalResult.error("用户不存在: ID=" + id);
        }
        return GlobalResult.success(user);
    }

    @PostMapping
    @Operation(summary = "新增用户", description = "添加新的Web用户")
    public GlobalResult add(@RequestBody WebUser webUser) {
        webUserService.save(webUser);
        return GlobalResult.success(null, "用户新增成功");
    }

    @PutMapping
    @Operation(summary = "更新用户", description = "根据ID更新Web用户信息（密码建议单独处理）")
    public GlobalResult update(@RequestBody WebUser webUser) {
        if (webUser.getId() == null) {
            return GlobalResult.error("更新时ID不能为空");
        }
        boolean ok = webUserService.updateById(webUser);
        return ok ? GlobalResult.success(null, "用户更新成功") : GlobalResult.error("用户不存在或更新失败");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户", description = "逻辑删除指定用户")
    public GlobalResult delete(@PathVariable Long id) {
        boolean ok = webUserService.removeById(id);
        return ok ? GlobalResult.success(null, "用户删除成功") : GlobalResult.error("用户不存在或删除失败");
    }
}
