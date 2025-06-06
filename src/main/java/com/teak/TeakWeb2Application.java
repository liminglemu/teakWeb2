package com.teak;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j
@EnableScheduling
@SpringBootApplication
@EnableTransactionManagement // 启用事务管理
@EnableAspectJAutoProxy()
@MapperScan(basePackages = "com.teak.mapper")
public class TeakWeb2Application {

    public static void main(String[] args) {
        SpringApplication.run(TeakWeb2Application.class, args);
        log.info("""
                    \s
                 _________   ________        _        ___  ____             ____      ____  ________   ______      _____  \s
                |  _   _  | |_   __  |      / \\      |_  ||_  _|           |_  _|    |_  _||_   __  | |_   _ \\    / ___ `.\s
                |_/ | | \\_|   | |_ \\_|     / _ \\       | |_/ /     ______    \\ \\  /\\  / /    | |_ \\_|   | |_) |  |_/___) |\s
                    | |       |  _| _     / ___ \\      |  __'.    |______|    \\ \\/  \\/ /     |  _| _    |  __'.   .'____.'\s
                   _| |_     _| |__/ |  _/ /   \\ \\_   _| |  \\ \\_               \\  /\\  /     _| |__/ |  _| |__) | / /_____ \s
                  |_____|   |________| |____| |____| |____||____|               \\/  \\/     |________| |_______/  |_______|\s
                
                """);
    }

}
