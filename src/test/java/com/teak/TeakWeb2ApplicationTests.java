package com.teak;

import com.teak.service.ScheduledTaskManager;
import com.teak.system.utils.TimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Lazy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired, @Lazy})
@SpringBootTest(classes = TeakWeb2Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TeakWeb2ApplicationTests {

    private final ScheduledTaskManager scheduledTaskManager;

    private final TimeUtils timeUtils;

    public static void main(String[] args) {
        Calendar instance = Calendar.getInstance();
        instance.set(1970, Calendar.FEBRUARY, 1, 0, 0, 0);
        log.info("{}", instance.getTime());
    }
    @Test
    void test3() {
        Calendar instance = Calendar.getInstance();
        instance.set(1970, Calendar.JANUARY, 1, 0, 0, 0);
        String string = timeUtils.dateToStringFormat(instance.getTime());
        log.info("{}", string);
        Date date = new Date(0L);
        log.info("{}", timeUtils.dateToStringFormat(date));
    }

    @Test
    void test1() {
        log.info("test");
    }

    @Test
    void contextLoads() {
    }

}
