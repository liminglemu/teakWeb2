package com.teak.service.impl;

import com.teak.model.dto.GenerateProgressReportResultDto;
import com.teak.service.AsyncReportService;
import com.teak.service.ExcelReportService;
import com.teak.system.utils.TeakUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * 异步报表服务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AsyncReportServiceImpl implements AsyncReportService {
    
    private final ExecutorService executorService;
    
    private final ExcelReportService excelReportService;
    
    private final TeakUtils teakUtils;
    
    /**
     * 异步生成生产进度报表
     * @param dataList 数据列表
     * @return CompletableFuture<Workbook> 异步结果
     */
    @Override
    public CompletableFuture<Workbook> generateProgressReportAsync(List<GenerateProgressReportResultDto> dataList) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("开始异步生成生产进度报表，线程: {}", Thread.currentThread().getName());
            try {
                // 模拟耗时操作
                Thread.sleep(2000);
                Workbook workbook = excelReportService.generateProgressReport(dataList);
                log.info("生产进度报表生成完成");
                return workbook;
            } catch (Exception e) {
                log.error("生成生产进度报表失败", e);
                throw new RuntimeException("生成报表失败", e);
            }
        }, executorService);
    }
    
    /**
     * 异步处理大量数据
     * @param data 数据列表
     * @return CompletableFuture<Boolean> 处理结果
     */
    @Override
    public CompletableFuture<Boolean> processDataAsync(List<String> data) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("开始异步处理数据，线程: {}", Thread.currentThread().getName());
            try {
                // 模拟数据处理
                for (String item : data) {
                    // 模拟处理每个数据项
                    log.info("处理数据项: {}", item);
                    Thread.sleep(100); // 模拟处理耗时
                }
                log.info("数据处理完成");
                return true;
            } catch (Exception e) {
                log.error("处理数据失败", e);
                return false;
            }
        }, executorService);
    }
    
    /**
     * 异步发送报表邮件
     * @param email 邮箱地址
     * @param subject 邮件主题
     * @param content 邮件内容
     * @return CompletableFuture<Boolean> 发送结果
     */
    @Override
    public CompletableFuture<Boolean> sendReportEmailAsync(String email, String subject, String content) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("开始异步发送邮件到: {}, 线程: {}", email, Thread.currentThread().getName());
            try {
                // 模拟邮件发送过程
                Thread.sleep(3000); // 模拟网络延迟
                log.info("邮件发送完成，收件人: {}, 主题: {}", email, subject);
                return true;
            } catch (Exception e) {
                log.error("发送邮件失败，收件人: {}", email, e);
                return false;
            }
        }, executorService);
    }
    
    /**
     * 组合多个异步任务示例
     * @param dataList 报表数据
     * @param email 邮箱地址
     * @return CompletableFuture<Boolean> 处理结果
     */
    public CompletableFuture<Boolean> generateReportAndSendEmailAsync(
            List<GenerateProgressReportResultDto> dataList, String email) {
        
        // 第一步：生成报表
        CompletableFuture<Workbook> reportFuture = generateProgressReportAsync(dataList);
        
        // 第二步：发送邮件（依赖第一步的结果）
        CompletableFuture<Boolean> emailFuture = reportFuture.thenCompose(workbook -> {
            log.info("报表生成完成，开始发送邮件");
            String subject = "生产进度报表";
            String content = "请查收附件中的生产进度报表";
            return sendReportEmailAsync(email, subject, content);
        });
        
        return emailFuture;
    }
    
    /**
     * 并行处理多个任务示例
     * @param dataList 数据列表
     * @return CompletableFuture<List<Boolean>> 处理结果列表
     */
    public CompletableFuture<List<Boolean>> processMultipleTasksInParallel(List<GenerateProgressReportResultDto> dataList) {
        // 并行执行多个任务
        CompletableFuture<Boolean> task1 = generateProgressReportAsync(dataList)
                .thenApply(workbook -> {
                    log.info("报表生成任务完成");
                    return true;
                })
                .exceptionally(throwable -> {
                    log.error("报表生成任务失败", throwable);
                    return false;
                });
        
        CompletableFuture<Boolean> task2 = processDataAsync(dataList.stream()
                .flatMap(dto -> dto.getResultList().stream())
                .map(Object::toString)
                .toList())
                .exceptionally(throwable -> {
                    log.error("数据处理任务失败", throwable);
                    return false;
                });
        
        // 等待所有任务完成
        return CompletableFuture.allOf(task1, task2)
                .thenApply(v -> List.of(task1.join(), task2.join()));
    }
}