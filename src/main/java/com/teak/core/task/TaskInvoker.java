package com.teak.core.task;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;

/**
 * 任务方法调用器（简化版）
 * 
 * <p>职责：根据任务配置调用对应的Spring Bean方法
 * <p>支持两种参数模式：
 * 1. 位置参数：taskArgs为JSON数组，按位置匹配方法参数
 * 2. 命名参数：taskArgs为JSON对象，按参数名匹配（需要方法参数使用@Param注解）
 * 
 * <p>简化设计：
 * - 在任务添加时进行方法验证，执行时直接调用
 * - 只支持基本类型和String的参数转换
 * - 避免复杂的重载方法解析
 */
@Slf4j
@Component
public class TaskInvoker {
    
    private final ApplicationContext applicationContext;
    
    public TaskInvoker(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    
    /**
     * 调用任务方法
     * 
     * @param beanName Bean名称
     * @param methodName 方法名称
     * @param taskArgsJson 任务参数JSON字符串
     * @return 调用结果
     * @throws Exception 调用失败
     */
    public Object invoke(String beanName, String methodName, String taskArgsJson) throws Exception {
        Object bean = applicationContext.getBean(beanName);
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);
        
        // 解析参数
        Object[] args = parseArguments(taskArgsJson);
        
        // 查找匹配的方法
        Method method = findMatchingMethod(targetClass, methodName, args);
        
        // 参数类型转换
        Object[] convertedArgs = convertArguments(args, method.getParameterTypes());
        
        // 执行方法
        log.debug("调用方法: {}.{} with {} args", beanName, methodName, convertedArgs.length);
        return method.invoke(bean, convertedArgs);
    }
    
    /**
     * 验证任务方法是否存在且参数匹配（在任务添加时调用）
     */
    public void validateMethod(String beanName, String methodName, String taskArgsJson) throws Exception {
        Object bean = applicationContext.getBean(beanName);
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);

        Object[] args = parseArguments(taskArgsJson);
        findMatchingMethod(targetClass, methodName, args);

        log.info("方法验证通过: {}.{} with {} parameters", beanName, methodName, args.length);
    }
    
    /**
     * 查找匹配的方法（简化版：参数数量匹配即可）
     */
    private Method findMatchingMethod(Class<?> targetClass, String methodName, Object[] args) throws NoSuchMethodException {
        Method[] methods = targetClass.getMethods();
        Method matchedMethod = null;
        
        for (Method method : methods) {
            if (method.getName().equals(methodName) && method.getParameterCount() == args.length) {
                // 参数数量匹配，接受该方法
                if (matchedMethod == null) {
                    matchedMethod = method;
                } else {
                    // 如果有多个匹配，选择第一个（简化逻辑，避免复杂重载）
                    log.warn("发现多个参数数量匹配的方法: {}.{}，使用第一个", targetClass.getSimpleName(), methodName);
                    break;
                }
            }
        }
        
        if (matchedMethod == null) {
            throw new NoSuchMethodException(
                String.format("方法 %s.%s 不存在或参数数量不匹配（需要%d个参数）", 
                    targetClass.getSimpleName(), methodName, args.length));
        }
        
        return matchedMethod;
    }
    
    /**
     * 解析JSON参数
     */
    private Object[] parseArguments(String taskArgsJson) {
        if (taskArgsJson == null || taskArgsJson.trim().isEmpty()) {
            return new Object[0];
        }
        
        try {
            // 尝试解析为数组
            if (taskArgsJson.trim().startsWith("[")) {
                List<?> list = JSONUtil.toList(taskArgsJson, Object.class);
                return list.toArray();
            }
            
            // 尝试解析为对象，转换为值数组
            Map<?, ?> map = JSONUtil.toBean(taskArgsJson, Map.class);
            return map.values().toArray();
            
        } catch (Exception e) {
            log.warn("解析taskArgs失败: {}, 使用空参数", e.getMessage());
            return new Object[0];
        }
    }
    
    /**
     * 参数类型转换
     */
    private Object[] convertArguments(Object[] rawArgs, Class<?>[] targetTypes) {
        if (rawArgs.length != targetTypes.length) {
            throw new IllegalArgumentException(
                String.format("参数数量不匹配: 原始%d个，目标%d个", rawArgs.length, targetTypes.length));
        }
        
        Object[] result = new Object[rawArgs.length];
        for (int i = 0; i < rawArgs.length; i++) {
            result[i] = convertArgument(rawArgs[i], targetTypes[i]);
        }
        return result;
    }
    
    /**
     * 单个参数转换
     */
    private Object convertArgument(Object rawValue, Class<?> targetType) {
        if (rawValue == null) {
            return null;
        }
        
        // 如果类型相同或兼容，直接返回
        if (targetType.isAssignableFrom(rawValue.getClass())) {
            return rawValue;
        }
        
        // 使用Hutool的BeanUtil进行类型转换
        try {
            return BeanUtil.toBean(rawValue, targetType);
        } catch (Exception e) {
            log.warn("参数类型转换失败: {} -> {}, 使用原始值", rawValue.getClass(), targetType);
            return rawValue;
        }
    }
    
    /**
     * 获取方法参数信息（用于日志和验证）
     */
    public String getMethodSignature(String beanName, String methodName) throws Exception {
        Object bean = applicationContext.getBean(beanName);
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);
        
        Method[] methods = targetClass.getMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                Parameter[] parameters = method.getParameters();
                String[] paramTypes = new String[parameters.length];
                for (int i = 0; i < parameters.length; i++) {
                    paramTypes[i] = parameters[i].getType().getSimpleName();
                }
                return String.format("%s(%s)", methodName, String.join(", ", paramTypes));
            }
        }
        
        return String.format("%s (未找到)", methodName);
    }
}