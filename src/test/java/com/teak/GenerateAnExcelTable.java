package com.teak;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.teak.model.dto.GenerateProgressReportResultDto;
import com.teak.model.dto.ProductionPlanMaintenanceQueryReportDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with: IntelliJ IDEA
 *
 * @Author: li zheng
 * @Date: 2025/12/2 10:00
 * @Project: teakWeb2
 * @File: GenerateAnExcelTable.java
 * @Description:
 */
@SpringBootTest
@Slf4j
class GenerateAnExcelTable {

    private CellStyle headerStyle;
    private CellStyle centerStyle;
    private CellStyle numberStyle;

    @Test
    void contextLoads() {

    }

    public List<GenerateProgressReportResultDto> GenerateProgressReportResultDto() {

        //7线-安规测试
        ProductionPlanMaintenanceQueryReportDto r7AnGui = new ProductionPlanMaintenanceQueryReportDto();
        r7AnGui.setProductionLineCode("R7");
        r7AnGui.setProductionLineName("7线");
        r7AnGui.setProcessCode("E090");
        r7AnGui.setProcessName("安规测试");
        r7AnGui.setAccumulatePlan(13);
        r7AnGui.setAccumulatedActual(13);
        r7AnGui.setCumulativeAchievementRate(new BigDecimal(100));
        r7AnGui.setPlanList(CollectionUtil.toList(
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 7, 6, 0, 0, 0, 0, 0
        ));
        r7AnGui.setActualList(CollectionUtil.toList(
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 7, 6, 0, 0, 0, 0, 0
        ));
        r7AnGui.setAchievementRateList(CollectionUtil.toList(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal(100), new BigDecimal(100), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
        ));

        //7线-上电测试
        ProductionPlanMaintenanceQueryReportDto r7ShangDian = new ProductionPlanMaintenanceQueryReportDto();
        r7ShangDian.setProductionLineCode("R7");
        r7ShangDian.setProductionLineName("7线");
        r7ShangDian.setProcessCode("E100");
        r7ShangDian.setProcessName("上电测试");
        r7ShangDian.setAccumulatePlan(14);
        r7ShangDian.setAccumulatedActual(14);
        r7ShangDian.setCumulativeAchievementRate(new BigDecimal(100));
        r7ShangDian.setPlanList(CollectionUtil.toList(
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 8, 6, 0, 0, 0, 0, 0
        ));
        r7ShangDian.setActualList(CollectionUtil.toList(
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 8, 6, 0, 0, 0, 0, 0
        ));
        r7ShangDian.setAchievementRateList(CollectionUtil.toList(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal(100), new BigDecimal(100), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
        ));

        //7线-成品出库
        ProductionPlanMaintenanceQueryReportDto r7ChengPin = new ProductionPlanMaintenanceQueryReportDto();
        r7ChengPin.setProductionLineCode("R7");
        r7ChengPin.setProductionLineName("7线");
        r7ChengPin.setProcessName("成品出库");
        r7ChengPin.setAccumulatePlan(15);
        r7ChengPin.setAccumulatedActual(15);
        r7ChengPin.setCumulativeAchievementRate(new BigDecimal(100));
        r7ChengPin.setPlanList(CollectionUtil.toList(
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 9, 6, 0, 0, 0, 0, 0
        ));
        r7ChengPin.setActualList(CollectionUtil.toList(
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 9, 6, 0, 0, 0, 0, 0
        ));
        r7ChengPin.setAchievementRateList(CollectionUtil.toList(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal(100), new BigDecimal(100), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
        ));

        //7线-安规测试
        ProductionPlanMaintenanceQueryReportDto r6AnGui = new ProductionPlanMaintenanceQueryReportDto();
        r6AnGui.setProductionLineCode("R6");
        r6AnGui.setProductionLineName("6线");
        r6AnGui.setProcessCode("E090");
        r6AnGui.setProcessName("安规测试");
        r6AnGui.setAccumulatePlan(8);
        r6AnGui.setAccumulatedActual(8);
        r6AnGui.setCumulativeAchievementRate(new BigDecimal(100));
        r6AnGui.setPlanList(CollectionUtil.toList(
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 8, 0, 0, 0, 0, 0
        ));
        r6AnGui.setActualList(CollectionUtil.toList(
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 8, 0, 0, 0, 0, 0
        ));
        r6AnGui.setAchievementRateList(CollectionUtil.toList(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal(100), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
        ));

        //6线-上电测试
        ProductionPlanMaintenanceQueryReportDto r6ShangDian = new ProductionPlanMaintenanceQueryReportDto();
        r6ShangDian.setProductionLineCode("R6");
        r6ShangDian.setProductionLineName("6线");
        r6ShangDian.setProcessCode("E100");
        r6ShangDian.setProcessName("上电测试");
        r6ShangDian.setAccumulatePlan(8);
        r6ShangDian.setAccumulatedActual(8);
        r6ShangDian.setCumulativeAchievementRate(new BigDecimal(100));
        r6ShangDian.setPlanList(CollectionUtil.toList(
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 8, 0, 0, 0, 0, 0
        ));
        r6ShangDian.setActualList(CollectionUtil.toList(
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 8, 0, 0, 0, 0, 0
        ));
        r6ShangDian.setAchievementRateList(CollectionUtil.toList(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal(100), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
        ));

        //7线-成品出库
        ProductionPlanMaintenanceQueryReportDto r6ChengPin = new ProductionPlanMaintenanceQueryReportDto();
        r6ChengPin.setProductionLineCode("R6");
        r6ChengPin.setProductionLineName("6线");
        r6ChengPin.setProcessName("成品出库");
        r6ChengPin.setAccumulatePlan(8);
        r6ChengPin.setAccumulatedActual(8);
        r6ChengPin.setCumulativeAchievementRate(new BigDecimal(100));
        r6ChengPin.setPlanList(CollectionUtil.toList(
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 8, 0, 0, 0, 0, 0
        ));
        r6ChengPin.setActualList(CollectionUtil.toList(
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 8, 0, 0, 0, 0, 0
        ));
        r6ChengPin.setAchievementRateList(CollectionUtil.toList(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal(100), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
        ));

        //小计-装配
        ProductionPlanMaintenanceQueryReportDto summaryAnGui = new ProductionPlanMaintenanceQueryReportDto();
        summaryAnGui.setProductionLineName("小计");
        summaryAnGui.setProcessName("装配");
        summaryAnGui.setAccumulatePlan(21);
        summaryAnGui.setAccumulatedActual(21);
        summaryAnGui.setCumulativeAchievementRate(new BigDecimal(100));
        summaryAnGui.setPlanList(CollectionUtil.toList(
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 7, 14, 0, 0, 0, 0, 0
        ));
        summaryAnGui.setActualList(CollectionUtil.toList(
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 7, 14, 0, 0, 0, 0, 0
        ));
        summaryAnGui.setAchievementRateList(CollectionUtil.toList(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal(100), new BigDecimal(100), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
        ));

        //小计-测试
        ProductionPlanMaintenanceQueryReportDto summaryShangDian = new ProductionPlanMaintenanceQueryReportDto();
        summaryShangDian.setProductionLineName("小计");
        summaryShangDian.setProcessName("测试");
        summaryShangDian.setAccumulatePlan(22);
        summaryShangDian.setAccumulatedActual(22);
        summaryShangDian.setCumulativeAchievementRate(new BigDecimal(100));
        summaryShangDian.setPlanList(CollectionUtil.toList(
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 8, 14, 0, 0, 0, 0, 0
        ));
        summaryShangDian.setActualList(CollectionUtil.toList(
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 8, 14, 0, 0, 0, 0, 0
        ));
        summaryShangDian.setAchievementRateList(CollectionUtil.toList(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal(100), new BigDecimal(100), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
        ));

        //小计-成品出库
        ProductionPlanMaintenanceQueryReportDto summaryChengPin = new ProductionPlanMaintenanceQueryReportDto();
        summaryChengPin.setProductionLineName("小计");
        summaryChengPin.setProcessName("成品出库");
        summaryChengPin.setAccumulatePlan(563);
        summaryChengPin.setAccumulatedActual(557);
        summaryChengPin.setCumulativeAchievementRate(new BigDecimal("98.93"));
        summaryChengPin.setPlanList(CollectionUtil.toList(
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 9, 554, 0, 0, 0, 0, 0
        ));
        summaryChengPin.setActualList(CollectionUtil.toList(
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 9, 548, 0, 0, 0, 0, 0
        ));
        summaryChengPin.setAchievementRateList(CollectionUtil.toList(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal(100), new BigDecimal("98.92"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
        ));

        ArrayList<ProductionPlanMaintenanceQueryReportDto> r7List = CollectionUtil.toList(r7AnGui, r7ShangDian, r7ChengPin);
        ArrayList<ProductionPlanMaintenanceQueryReportDto> r6List = CollectionUtil.toList(r6AnGui, r6ShangDian, r6ChengPin);
        ArrayList<ProductionPlanMaintenanceQueryReportDto> summaryList = CollectionUtil.toList(summaryAnGui, summaryShangDian, summaryChengPin);

        return CollectionUtil.list(false, new GenerateProgressReportResultDto() {{
            setResultList(r7List);
        }}, new GenerateProgressReportResultDto() {{
            setResultList(r6List);
        }}, new GenerateProgressReportResultDto() {{
            setResultList(summaryList);
        }});
    }

    public static void main(String[] args) {
        String outPutFilePath = "C:/Users/14014/Desktop/生产进度报表导出.xlsx";

        GenerateAnExcelTable generateAnExcelTable = new GenerateAnExcelTable();
        List<GenerateProgressReportResultDto> generateProgressReportResultDtos = generateAnExcelTable.GenerateProgressReportResultDto();
        Workbook sheets = generateAnExcelTable.generateAnExcelTable(generateProgressReportResultDtos);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(outPutFilePath);
            sheets.write(fileOutputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info("生成报表成功");
    }


    public Workbook generateAnExcelTable(List<GenerateProgressReportResultDto> dataList) {
        if (CollectionUtil.isEmpty(dataList)) {
            return new XSSFWorkbook();
        }

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("生产进度报表");

        //初始化样式
        headerStyle = createHeaderStyle(workbook);
        centerStyle = createCenterStyle(workbook);
        numberStyle = createNumberStyle(workbook);

        //获取每日列数
        int dailColumCount = getDailColumCount(dataList);
        if (dailColumCount <= 0) {
            log.error("数据为空,默认使用10列");
            dailColumCount = 10;
        }

        //创建表头
        createHeader(sheet, dailColumCount);
        //填充数据
        fillData(sheet, dataList, dailColumCount);
        //冻结前4列A-D列
        sheet.createFreezePane(4, 0);
        //设置列宽
        setColumnWidths(sheet, dailColumCount);

        return workbook;
    }

    private XSSFCellStyle createHeaderStyle(XSSFWorkbook workbook) {
        XSSFCellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setWrapText(true);

        XSSFFont font = workbook.createFont();
        font.setBold(true);
        cellStyle.setFont(font);

        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);

        return cellStyle;
    }

    private XSSFCellStyle createCenterStyle(XSSFWorkbook workbook) {
        XSSFCellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setWrapText(true);

        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);

        return cellStyle;
    }

    private XSSFCellStyle createNumberStyle(XSSFWorkbook workbook) {
        XSSFCellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);

        return cellStyle;
    }

    private int getDailColumCount(List<GenerateProgressReportResultDto> dataList) {
        if (CollectionUtil.isEmpty(dataList)) {
            return 0;
        }
        List<Integer> planList = dataList.get(0).getResultList().get(0).getPlanList();

        return CollectionUtil.isEmpty(planList) ? 0 : planList.size();
    }

    private void createHeader(XSSFSheet sheet, int dailyColumCount) {
        XSSFRow row1 = sheet.createRow(0);
        XSSFRow row2 = sheet.createRow(1);

        //A1-A2:产线
        XSSFCell cellA1 = row1.createCell(0);
        cellA1.setCellValue("产线");
        cellA1.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 1, 0, 0));

        //B1-B2:工序
        XSSFCell cellB1 = row1.createCell(1);
        cellB1.setCellValue("工序");
        cellB1.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 1, 1, 1));

        //C1-D2:累计
        XSSFCell cellC1 = row1.createCell(2);
        cellC1.setCellValue("累计");
        cellC1.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 1, 2, 3));

        //E1每日单项明细
        XSSFCell cellE1 = row1.createCell(4);
        cellE1.setCellValue("每日单项明细");
        cellE1.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 4, 4 + dailyColumCount - 1));

        //E2 开始：天数编号
        for (int i = 0; i < dailyColumCount; i++) {
            XSSFCell cellE2 = row2.createCell(4 + i);
            cellE2.setCellValue(i + 1);
            cellE2.setCellStyle(headerStyle);
        }
    }

    private void fillData(XSSFSheet sheet, List<GenerateProgressReportResultDto> dataList, int dailyColumCount) {
        //从第3行开始写数据
        int currentRow = 2;

        for (GenerateProgressReportResultDto container : dataList) {
            List<ProductionPlanMaintenanceQueryReportDto> resultList = container.getResultList();
            if (CollectionUtil.isEmpty(resultList)) {
                continue;
            }

            String lineName = getLineDisplayName(resultList.get(0));
            int mergeEmdRow = currentRow + 3 * resultList.size() - 1;

            XSSFCell lineCell;
            if (ObjUtil.isEmpty(sheet.getRow(currentRow))) {
                XSSFRow row = sheet.createRow(currentRow);
                lineCell = row.createCell(0);
            } else {
                lineCell = sheet.getRow(currentRow).createCell(0);
            }

            lineCell.setCellValue(lineName);
            lineCell.setCellStyle(centerStyle);

            sheet.addMergedRegion(new CellRangeAddress(currentRow, mergeEmdRow, 0, 0));

            //填充每个工序
            for (ProductionPlanMaintenanceQueryReportDto item : resultList) {
                fillSingleRow(sheet, item, currentRow, dailyColumCount);
                currentRow += 3;
            }
        }
    }

    private String getLineDisplayName(ProductionPlanMaintenanceQueryReportDto item) {
        if (StrUtil.isNotEmpty(item.getProductionLineCode())) {
            return item.getProductionLineCode() + "-" + item.getProductionLineName();
        }
        return item.getProductionLineName();
    }

    private void fillSingleRow(XSSFSheet sheet, ProductionPlanMaintenanceQueryReportDto item, int rowStart, int dailyColumCount) {
        if (ObjUtil.isEmpty(item)) {
            return;
        }

        //工序名称，合并3行
        XSSFCell processCell;
        if (ObjUtil.isEmpty(sheet.getRow(rowStart))) {
            XSSFRow row = sheet.createRow(rowStart);
            processCell = row.createCell(1);
        } else {
            processCell = sheet.getRow(rowStart).createCell(1);
        }
        String processName = item.getProcessName();
        if (StrUtil.isNotEmpty(item.getProductionLineCode()) && !StrUtil.equals("小计", item.getProductionLineName())) {
            processName = item.getProductionLineCode() + "-" + processName;
        }
        processCell.setCellValue(processName);
        sheet.addMergedRegion(new CellRangeAddress(rowStart, rowStart + 2, 1, 1));
        processCell.setCellStyle(centerStyle);

        //累计部分：C3=计划，C4=实际，C5=达成率
        XSSFRow planRow = sheet.getRow(rowStart);
        XSSFRow actualRow = sheet.createRow(rowStart + 1);
        XSSFRow rateRow = sheet.createRow(rowStart + 2);

        XSSFCell planRowCell = planRow.createCell(2);
        planRowCell.setCellValue("计划");
        planRowCell.setCellStyle(centerStyle);
        XSSFCell actualRowCell = actualRow.createCell(2);
        actualRowCell.setCellValue("实际");
        actualRowCell.setCellStyle(centerStyle);
        XSSFCell rateRowCell = rateRow.createCell(2);
        rateRowCell.setCellValue("达成率");
        rateRowCell.setCellStyle(centerStyle);

        //D列：累计值
        XSSFCell planRowCell1 = planRow.createCell(3);
        planRowCell1.setCellValue(item.getAccumulatePlan());
        planRowCell1.setCellStyle(numberStyle);
        XSSFCell actualRowCell1 = actualRow.createCell(3);
        actualRowCell1.setCellValue(item.getAccumulatedActual());
        actualRowCell1.setCellStyle(numberStyle);
        XSSFCell rateRowCell1 = rateRow.createCell(3);
        rateRowCell1.setCellValue(item.getCumulativeAchievementRate().toString());
        rateRowCell1.setCellStyle(numberStyle);

        //E列：每日数据
        fillDailyDate(planRow, item.getPlanList(), dailyColumCount, numberStyle);
        fillDailyDate(actualRow, item.getActualList(), dailyColumCount, numberStyle);
        fillDailyDate(rateRow, item.getAchievementRateList(), dailyColumCount, numberStyle);
    }

    private void fillDailyDate(XSSFRow row, List<? extends Number> dataList, int dailyColumCount, CellStyle numberStyle) {
        if (CollectionUtil.isEmpty(dataList)) {
            //填充空值占位
            for (int i = 0; i < dailyColumCount; i++) {
                row.createCell(4 + i).setCellStyle(numberStyle);
            }
            return;
        }

        for (int i = 0; i < dailyColumCount; i++) {
            Number value = dataList.get(i);
            XSSFCell cell = row.createCell(4 + i);
            if (ObjUtil.isNotEmpty(value)) {
                if (value instanceof Integer) {
                    cell.setCellValue(value.intValue());
                } else {
                    cell.setCellValue(value.doubleValue());
                }
            }
            cell.setCellStyle(numberStyle);
        }
    }

    private void setColumnWidths(XSSFSheet sheet, int dailyColumCount) {
        sheet.setColumnWidth(0, 4000);//A列
        sheet.setColumnWidth(0, 5000);//B列
        sheet.setColumnWidth(0, 2000);//C列
        sheet.setColumnWidth(0, 3000);//D列

        for (int i = 0; i < dailyColumCount; i++) {
            sheet.setColumnWidth(i + 4, 2000);
        }
    }
}
