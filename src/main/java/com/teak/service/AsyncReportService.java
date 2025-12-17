package com.teak.service;

import com.teak.model.dto.GenerateProgressReportResultDto;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 异步报表服务接口
 */
public interface AsyncReportService {
    
    /**
     * 异步生成生产进度报表
     * @param dataList 数据列表
     * @return CompletableFuture<Workbook> 异步结果
     */
    CompletableFuture<Workbook> generateProgressReportAsync(List<GenerateProgressReportResultDto> dataList);
    
    /**
     * 异步处理大量数据
     * @param data 数据列表
     * @return CompletableFuture<Boolean> 处理结果
     */
    CompletableFuture<Boolean> processDataAsync(List<String> data);
    
    /**
     * 异步发送报表邮件
     * @param email 邮箱地址
     * @param subject 邮件主题
     * @param content 邮件内容
     * @return CompletableFuture<Boolean> 发送结果
     */
    CompletableFuture<Boolean> sendReportEmailAsync(String email, String subject, String content);
}