package com.teak.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teak.model.SysScheduledTask;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Created with: IntelliJ IDEA
 *
 * @Author: li zheng
 * @Date: 2025/4/19 10:21
 * @Project: teakWeb
 * @File: SysScheduledTaskMapper.java
 * @Description:
 */
@Mapper
public interface SysScheduledTaskMapper extends BaseMapper<SysScheduledTask> {
    List<SysScheduledTask> getAllTask();

    List<SysScheduledTask> findByStatus(int i);
}
