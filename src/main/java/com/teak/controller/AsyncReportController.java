package com.teak.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.stream.StreamUtil;
import cn.hutool.core.util.StrUtil;
import com.teak.model.dto.GenerateProgressReportResultDto;
import com.teak.service.AsyncReportService;
import com.teak.service.ExcelReportService;
import com.teak.service.impl.AsyncReportServiceImpl;
import com.teak.system.result.GlobalResult;
import com.teak.util.DataGeneratorUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 异步报表控制器
 */
@RestController
@RequestMapping("/api/async/report")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "异步报表", description = "异步生成报表相关接口")
public class AsyncReportController {

    private final AsyncReportService asyncReportService;

    private final AsyncReportServiceImpl asyncReportServiceImpl;

    private final ExcelReportService excelReportService;

    private final DataGeneratorUtil dataGeneratorUtil;

    /**
     * 异步生成报表并返回任务ID
     *
     * @return 任务ID
     */
    @PostMapping("/generateProgressReport")
    @Operation(summary = "异步生成报表", description = "异步生成报表并返回任务ID")
    public GlobalResult generateProgressReportAsync() {
        log.info("收到异步生成报表请求");

        // 获取测试数据
        List<GenerateProgressReportResultDto> testData = excelReportService.getProgressReportTestData();

        // 异步生成报表
        CompletableFuture<Void> future = asyncReportService.generateProgressReportAsync(testData)
                .thenAccept(workbook -> {
                    log.info("报表生成完成，准备保存或发送");
                    // 这里可以保存文件到磁盘或上传到OSS等
                })
                .exceptionally(throwable -> {
                    log.error("报表生成失败", throwable);
                    return null;
                });

        // 返回任务提交成功的响应
        return GlobalResult.success("报表生成任务已提交");
    }

    /**
     * 异步处理数据示例
     *
     * @param data 数据列表
     * @return 处理结果
     */
    @PostMapping("/processData")
    @Operation(summary = "异步处理数据", description = "异步处理数据示例")
    public GlobalResult processDataAsync(@Parameter(description = "待处理的数据列表") @RequestBody List<String> data) {

        if (CollectionUtil.isEmpty(data)) {
            data = dataGeneratorUtil.generateRandomData(1000);
        }

        log.info("收到异步处理数据请求，数据量: {}", data.size());

        CompletableFuture<Void> future = asyncReportService.processDataAsync(data)
                .thenAccept(result -> {
                    if (result) {
                        log.info("数据处理成功");
                    } else {
                        log.error("数据处理失败");
                    }
                })
                .exceptionally(throwable -> {
                    log.error("数据处理过程中发生异常", throwable);
                    return null;
                });

        return GlobalResult.success("数据处理任务已提交");
    }

    /**
     * 异步发送邮件示例
     *
     * @param email   邮箱地址
     * @param subject 邮件主题
     * @param content 邮件内容
     * @return 发送结果
     */
    @PostMapping("/sendEmail")
    @Operation(summary = "异步发送邮件", description = "异步发送邮件示例")
    public GlobalResult sendEmailAsync(@Parameter(description = "邮箱地址") @RequestParam String email,
                                       @Parameter(description = "邮件主题") @RequestParam String subject,
                                       @Parameter(description = "邮件内容") @RequestParam String content) {
        log.info("收到异步发送邮件请求，收件人: {}", email);

        CompletableFuture<Void> future = asyncReportService.sendReportEmailAsync(email, subject, content)
                .thenAccept(result -> {
                    if (result) {
                        log.info("邮件发送成功");
                    } else {
                        log.error("邮件发送失败");
                    }
                })
                .exceptionally(throwable -> {
                    log.error("邮件发送过程中发生异常", throwable);
                    return null;
                });

        return GlobalResult.success("邮件发送任务已提交");
    }

    /**
     * 组合任务示例：生成报表并发送邮件
     *
     * @param email 邮箱地址
     * @return 处理结果
     */
    @PostMapping("/generateAndSend")
    @Operation(summary = "生成报表并发送邮件", description = "组合任务示例：生成报表并发送邮件")
    public GlobalResult generateReportAndSendEmail(@Parameter(description = "邮箱地址") @RequestParam String email) {
        log.info("收到组合任务请求：生成报表并发送邮件，收件人: {}", email);

        // 获取测试数据
        List<GenerateProgressReportResultDto> testData = excelReportService.getProgressReportTestData();

        // 执行组合任务
        CompletableFuture<Void> future = asyncReportServiceImpl.generateReportAndSendEmailAsync(testData, email)
                .thenAccept(result -> {
                    if (result) {
                        log.info("组合任务执行成功");
                    } else {
                        log.error("组合任务执行失败");
                    }
                })
                .exceptionally(throwable -> {
                    log.error("组合任务执行过程中发生异常", throwable);
                    return null;
                });

        return GlobalResult.success("组合任务已提交");
    }

    /**
     * 并行任务示例：同时执行多个任务
     *
     * @return 处理结果
     */
    @PostMapping("/parallelTasks")
    @Operation(summary = "执行并行任务", description = "并行任务示例：同时执行多个任务")
    public GlobalResult executeParallelTasks() {
        log.info("收到并行任务请求");

        // 获取测试数据
        List<GenerateProgressReportResultDto> testData = excelReportService.getProgressReportTestData();

        // 执行并行任务
        CompletableFuture<Void> future = asyncReportServiceImpl.processMultipleTasksInParallel(testData)
                .thenAccept(results -> {
                    log.info("并行任务执行完成，结果: {}", results);
                })
                .exceptionally(throwable -> {
                    log.error("并行任务执行过程中发生异常", throwable);
                    return null;
                });

        return GlobalResult.success("并行任务已提交");
    }
}