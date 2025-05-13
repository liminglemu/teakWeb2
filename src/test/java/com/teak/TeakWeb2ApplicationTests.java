package com.teak;

import com.teak.model.vo.SysScheduledTaskVo;
import com.teak.service.SysScheduledTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Lazy;

import java.io.Serializable;
import java.util.ArrayList;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired, @Lazy})
@SpringBootTest(classes = TeakWeb2Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TeakWeb2ApplicationTests {

    private final SysScheduledTaskService sysScheduledTaskService;

    public static void main(String[] args) {

    }

    @Test
    void test2() {
        SysScheduledTaskVo sysScheduledTaskVo = new SysScheduledTaskVo();
        sysScheduledTaskVo.setTaskName("设备故障记录服务");
        sysScheduledTaskVo.setBeanName("deviceFaultRecordsServiceImpl");
        sysScheduledTaskVo.setMethodName("getDeviceFaultRecords");
        sysScheduledTaskVo.setCronExpression("0/5 * * * * ?");
        ArrayList<Serializable> arrayList = new ArrayList<>();
        arrayList.add("2020-01-01 00:00:00");
        arrayList.add("2025-02-01 12:30:30");
        sysScheduledTaskVo.setParams(arrayList);
        sysScheduledTaskVo.setParameterTypes("String,String");
        sysScheduledTaskVo.setStatus(1);
        sysScheduledTaskService.addScheduledTask(sysScheduledTaskVo);
    }

    @Test
    void test1() {
        log.info("test");
    }

    @Test
    void contextLoads() {
    }

}
