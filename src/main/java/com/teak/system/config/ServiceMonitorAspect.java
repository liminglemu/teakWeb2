package com.teak.system.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * Created with: IntelliJ IDEA
 *
 * @Author: li zheng
 * @Date: 2025/3/4 21:45
 * @Project: teakWeb
 * @File: ServiceMonitorAspect.java
 * @Description:
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class ServiceMonitorAspect {

    private static final ObjectMapper mapper = new ObjectMapper();


    @Pointcut("execution(public * com.teak.service.*.*(..))")
    public void ServiceMonitor() {
    }

    @Around("ServiceMonitor()")
    public Object unifiedMonitor(ProceedingJoinPoint joinPoint) throws Throwable {
        // 方法签名记录
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        String[] paramNames = signature.getParameterNames(); // 获取参数名称数组
        Object[] paramValues = joinPoint.getArgs(); // 获取参数值数组

        // 构建参数键值对
        Map<String, Object> paramMap = new LinkedHashMap<>();
        for (int i = 0; i < paramNames.length; i++) {
            paramMap.put(paramNames[i], paramValues[i]);
        }

        String className = signature.getDeclaringTypeName();
        String methodName = signature.getName();
        String fullMethodName = className + "::" + methodName;

        // 参数记录
        String params = mapper.writeValueAsString(paramMap);

        log.info("方法 [{}] 参数详情: {}", fullMethodName, params);

        // 精准计时开始
        long nanoStart = System.nanoTime();
        Object result = joinPoint.proceed();  // 仅测量业务方法
        long nanoCost = (System.nanoTime() - nanoStart) / 1_000_000;

        log.info("[性能监控] {}.{} 执行耗时: {}ms", className, methodName, nanoCost);

        // 统一使用纳秒计时
        // 超时告警
        if (nanoCost > 1000) {
            log.warn("{} 超时警告！耗时 {} ms", signature.toShortString(), nanoCost);
        }

        return result;
    }

}
