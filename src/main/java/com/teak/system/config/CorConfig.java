package com.teak.system.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * Created with: IntelliJ IDEA
 *
 * @Author: li zheng
 * @Date: 2025/2/9 16:37
 * @Project: teakWeb
 * @File: CorConfig.java
 * @Description:
 */
@Slf4j
@Configuration
public class CorConfig {

    /*@Bean
    public WebMvcConfigurer configurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NotNull CorsRegistry registry) {
                try {
                    registry.addMapping("/admin/**")
                            .allowedOrigins("http://localhost:63342")
                            .allowedMethods("GET", "POST")
                            .allowedHeaders("Content-Type", "Authorization")
                            .allowCredentials(true)
                            .maxAge(3600);
                } catch (Exception e) {
                    log.error("CORS配置失败", e);
                }

            }
        };
    }*/
}
