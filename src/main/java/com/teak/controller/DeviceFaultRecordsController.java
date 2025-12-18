package com.teak.controller;

import com.teak.model.vo.DeviceFaultRecordsVo;
import com.teak.service.DeviceFaultRecordsService;
import com.teak.system.result.GlobalResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;

/**
 * Created with: IntelliJ IDEA
 *
 * @Author: li zheng
 * @Date: 2025/3/11 21:33
 * @Project: teakWeb
 * @File: DeviceFaultRecordsController.java
 * @Description:
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/deviceFaultRecords")
@Tag(name = "设备故障记录", description = "设备故障记录相关接口")
public class DeviceFaultRecordsController {

    private final DeviceFaultRecordsService deviceFaultRecordsService;

    @GetMapping("/getDeviceFaultRecords")
    @Operation(summary = "获取设备故障记录", description = "根据时间范围获取设备故障记录")
    public GlobalResult getDeviceFaultRecords(@Parameter(description = "开始时间，格式: yyyy-MM-dd HH:mm:ss") @RequestParam String startTime, 
                                             @Parameter(description = "结束时间，格式: yyyy-MM-dd HH:mm:ss") @RequestParam String endTime) {
        List<DeviceFaultRecordsVo> deviceFaultRecords = deviceFaultRecordsService.getDeviceFaultRecords(startTime, endTime);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("deviceFaultRecords", deviceFaultRecords);
        return GlobalResult.success(hashMap);
    }
}
