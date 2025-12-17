package com.teak.service;

import com.teak.model.dto.GenerateProgressReportResultDto;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;

/**
 * Excel报表服务接口
 */
public interface ExcelReportService {
    
    /**
     * 生成生产进度报表
     * @param dataList 数据列表
     * @return Excel工作簿
     */
    Workbook generateProgressReport(List<GenerateProgressReportResultDto> dataList);
    
    /**
     * 获取生产进度报表测试数据
     * @return 测试数据列表
     */
    List<GenerateProgressReportResultDto> getProgressReportTestData();
}