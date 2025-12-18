package com.teak.controller;

import com.teak.model.dto.GenerateProgressReportResultDto;
import com.teak.service.ExcelReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Excel报表控制器
 */
@RestController
@RequestMapping("/api/excel")
@RequiredArgsConstructor
@Tag(name = "Excel报表", description = "Excel报表生成相关接口")
public class ExcelReportController {

    private final ExcelReportService excelReportService;

    /**
     * 生成生产进度报表Excel文件
     *
     * @param response HTTP响应对象
     */
    @GetMapping("/generateProgressReport")
    @Operation(summary = "生成生产进度报表", description = "生成生产进度报表Excel文件")
    public void generateProgressReport(HttpServletResponse response) {
        try {
            // 获取测试数据
            List<GenerateProgressReportResultDto> testData = excelReportService.getProgressReportTestData();

            // 生成Excel工作簿
            Workbook workbook = excelReportService.generateProgressReport(testData);

            // 设置响应头信息
            String fileName = URLEncoder.encode("生产进度报表.xlsx", StandardCharsets.UTF_8);
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

            // 将Excel写入响应输出流
            workbook.write(response.getOutputStream());
            workbook.close();
        } catch (IOException e) {
            throw new RuntimeException("生成Excel报表失败", e);
        }
    }
}