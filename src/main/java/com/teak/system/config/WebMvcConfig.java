package com.teak.system.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.teak.system.config.converter.LongToStringConverter;
import com.teak.system.config.converter.SimpleDateFormatConverter;
import com.teak.system.config.interceptor.CustomInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Date;
import java.util.List;

/**
 * Created with: IntelliJ IDEA
 *
 * @Author: li zheng
 * @Date: 2025/3/8 12:54
 * @Project: teakWeb
 * @File: WebMvcConfig.java
 * @Description:
 */
@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CustomInterceptor())
                .addPathPatterns("/**")// 拦截所有请求
                .excludePathPatterns(
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**",
                        "/actuator/**"
                ); // 排除Swagger和Actuator相关路径
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 在现有转换器基础上扩展，而非替换全部，避免影响SpringDoc等框架的序列化
        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof MappingJackson2HttpMessageConverter jacksonConverter) {
                ObjectMapper objectMapper = jacksonConverter.getObjectMapper();
                SimpleModule module = new SimpleModule();
                module.addSerializer(Long.class, new LongToStringConverter());
                module.addSerializer(Date.class, new SimpleDateFormatConverter());
                objectMapper.registerModule(module);
                break; // 只修改第一个Jackson转换器即可
            }
        }
    }
}
