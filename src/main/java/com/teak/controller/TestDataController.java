package com.teak.controller;

import com.teak.service.AsyncReportService;
import com.teak.system.result.GlobalResult;
import com.teak.util.DataGeneratorUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 测试数据控制器
 */
@RestController
@RequestMapping("/api/test")
@Slf4j
@RequiredArgsConstructor
public class TestDataController {
    
    private final DataGeneratorUtil dataGeneratorUtil;
    
    private final AsyncReportService asyncReportService;
    
    /**
     * 生成指定数量的随机测试数据
     * @param count 数据数量
     * @return 随机数据列表
     */
    @GetMapping("/generateData")
    public GlobalResult generateTestData(@RequestParam(defaultValue = "1000") int count) {
        log.info("收到生成测试数据请求，数量: {}", count);
        
        List<String> testData = dataGeneratorUtil.generateRandomData(count);
        return GlobalResult.success(testData);
    }
    
    /**
     * 生成并处理指定数量的随机测试数据
     * @param count 数据数量
     * @return 处理结果
     */
    @PostMapping("/generateAndProcessData")
    public GlobalResult generateAndProcessData(@RequestParam(defaultValue = "1000") int count) {
        log.info("收到生成并处理测试数据请求，数量: {}", count);
        
        List<String> testData = dataGeneratorUtil.generateRandomData(count);
        
        // 异步处理数据
        asyncReportService.processDataAsync(testData)
                .thenAccept(result -> {
                    if (result) {
                        log.info("数据处理成功，数量: {}", testData.size());
                    } else {
                        log.error("数据处理失败");
                    }
                })
                .exceptionally(throwable -> {
                    log.error("数据处理过程中发生异常", throwable);
                    return null;
                });
        
        return GlobalResult.success("已提交处理" + count + "条数据的任务");
    }
}