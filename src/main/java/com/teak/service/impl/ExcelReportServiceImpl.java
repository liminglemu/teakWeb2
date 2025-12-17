package com.teak.service.impl;

import com.teak.model.dto.GenerateProgressReportResultDto;
import com.teak.service.ExcelReportService;
import com.teak.util.ExcelGeneratorUtil;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Excel报表服务实现类
 * 使用策略模式设计，便于扩展不同类型的Excel报表生成策略
 */
@Service
@RequiredArgsConstructor
public class ExcelReportServiceImpl implements ExcelReportService {
    
    private final ExcelGeneratorUtil excelGeneratorUtil;
    
    /**
     * 生成生产进度报表
     * @param dataList 数据列表
     * @return Excel工作簿
     */
    @Override
    public Workbook generateProgressReport(List<GenerateProgressReportResultDto> dataList) {
        return excelGeneratorUtil.generateProgressReportWorkbook(dataList);
    }
    
    /**
     * 获取生产进度报表测试数据
     * @return 测试数据列表
     */
    @Override
    public List<GenerateProgressReportResultDto> getProgressReportTestData() {
        return excelGeneratorUtil.createTestData();
    }
}