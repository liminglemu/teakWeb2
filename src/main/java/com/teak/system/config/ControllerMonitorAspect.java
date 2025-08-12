package com.teak.system.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Created with: IntelliJ IDEA
 *
 * @Author: li zheng
 * @Date: 2025/6/11 18:28
 * @Project: teakWeb2
 * @File: ControllerMonitorAspect.java
 * @Description:
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class ControllerMonitorAspect {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Pointcut("execution(public * com.teak.controller.*.*(..))")
    public void ControllerMonitor() {

    }

    @Around("ControllerMonitor()")
    public Object unifiedMonitor(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String remoteAddr = request.getRemoteAddr(); // 客户端IP
            String requestURL = request.getRequestURL().toString(); // 请求的URL

            log.info("请求地址: {}, 客户端IP: {}", requestURL, remoteAddr);
        }
        return joinPoint.proceed();
    }
}
