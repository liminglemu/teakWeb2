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
                    ___                                    ,-.             .---.                             ,----,  \s
                  ,--.'|_                              ,--/ /|            /. ./|              ,---,        .'   .' \\ \s
                  |  | :,'                           ,--. :/ |        .--'.  ' ;            ,---.'|      ,----,'    |\s
                  :  : ' :                           :  : ' /        /__./ \\ : |            |   | :      |    :  .  ;\s
                .;__,'  /      ,---.      ,--.--.    |  '  /     .--'.  '   \\' .    ,---.   :   : :      ;    |.'  / \s
                |  |   |      /     \\    /       \\   '  |  :    /___/ \\ |    ' '   /     \\  :     |,-.   `----'/  ;  \s
                :__,'| :     /    /  |  .--.  .-. |  |  |   \\   ;   \\  \\;      :  /    /  | |   : '  |     /  ;  /   \s
                  '  : |__  .    ' / |   \\__\\/: . .  '  : |. \\   \\   ;  `      | .    ' / | |   |  / :    ;  /  /-,  \s
                  |  | '.'| '   ;   /|   ," .--.; |  |  | ' \\ \\   .   \\    .\\  ; '   ;   /| '   : |: |   /  /  /.`|  \s
                  ;  :    ; '   |  / |  /  /  ,.  |  '  : |--'     \\   \\   ' \\ | '   |  / | |   | '/ : ./__;      :  \s
                  |  ,   /  |   :    | ;  :   .'   \\ ;  |,'         :   '  |--"  |   :    | |   :    | |   :    .'   \s
                   ---`-'    \\   \\  /  |  ,     .-./ '--'            \\   \\ ;      \\   \\  /  /    \\  /  ;   | .'      \s
                              `----'    `--`---'                      '---"        `----'   `-'----'   `---'         \s
                
                """);
    }

}
