package com.teak.model.dto;

import lombok.Data;
import org.springframework.data.relational.core.sql.In;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Created with: IntelliJ IDEA
 *
 * @Author: li zheng
 * @Date: 2025/12/2 10:08
 * @Project: teakWeb2
 * @File: ProductionPlanMaintenanceQueryReportDto.java
 * @Description:
 */
@Data
public class ProductionPlanMaintenanceQueryReportDto {
    /**
     * 产线编码
     */
    private String productionLineCode;

    /**
     * 产线名称
     */
    private String productionLineName;

    /**
     * 工序编码
     */
    private String processCode;

    /**
     * 工序名称
     */
    private String processName;

    /**
     * 计划数
     */
    private Integer plan;

    /**
     * 实际数
     */
    private Integer actual;

    /**
     * 成功率
     */
    private BigDecimal achievementRate;

    /**
     * 累计计划数
     */
    private Integer accumulatePlan;

    /**
     * 累计实际数
     */
    private Integer accumulatedActual;

    /**
     * 累计成功率
     */
    private BigDecimal cumulativeAchievementRate;

    /**
     * 计划数列表
     */
    private List<Integer> planList;

    /**
     * 实际数列表
     */
    private List<Integer> actualList;

    /**
     * 成功率列表
     */
    private List<BigDecimal> achievementRateList;

    /**
     * 执行时间
     */
    private Instant executionDate;
}
