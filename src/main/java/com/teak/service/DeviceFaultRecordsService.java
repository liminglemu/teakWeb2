package com.teak.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.teak.model.DeviceFaultRecords;
import com.teak.model.vo.DeviceFaultRecordsVo;

import java.util.List;

/**
 * Created with: IntelliJ IDEA
 *
 * @Author: li zheng
 * @Date: 2025/3/11 21:34
 * @Project: teakWeb
 * @File: DeviceFaultRecordsService.java
 * @Description:
 */
public interface DeviceFaultRecordsService extends IService<DeviceFaultRecords> {
    List<DeviceFaultRecordsVo> getDeviceFaultRecords(String startTime, String endTime);
}
