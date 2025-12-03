package com.teak.model.dto;

import lombok.Data;

import java.util.List;

/**
 * Created with: IntelliJ IDEA
 *
 * @Author: li zheng
 * @Date: 2025/12/2 10:07
 * @Project: teakWeb2
 * @File: GenerateProgressReportResultDto.java
 * @Description:
 */
@Data
public class GenerateProgressReportResultDto {
    private List<ProductionPlanMaintenanceQueryReportDto>resultList;
}
