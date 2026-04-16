package com.teak.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teak.mapper.SysScheduledTaskMapper;
import com.teak.model.SysScheduledTask;
import com.teak.model.vo.SysScheduledTaskVo;
import com.teak.service.ScheduledTaskManager;
import com.teak.system.event.TaskRefreshEvent;
import com.teak.system.utils.TeakUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.context.ApplicationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * 定时任务管理器实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledTaskManagerImpl implements ScheduledTaskManager {

    private final SysScheduledTaskMapper sysScheduledTaskMapper;
    private final TeakUtils teakUtils;
    private final ApplicationContext applicationContext;
    private final ObjectMapper objectMapper;
    /**
     * 参数名发现器，用于获取方法参数名
     */
    private static final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    @Override
    public List<SysScheduledTask> getAllTasks() {
        return sysScheduledTaskMapper.getAllTask();
    }

    /**
     * 添加并启动定时任务（增强版：自动解析 + 预校验 + taskArgs智能参数）
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addAndStartScheduledTask(SysScheduledTaskVo vo) {
        // 1. 解析 methodPath（快速模式）
        ResolveMethodPathResult resolveMethodPathResult = resolveMethodPath(vo);

        // 2. 标准化字段
        String beanName = teakUtils.lowerFirstCharAndTrim(resolveMethodPathResult.beanName());
        String methodName = teakUtils.lowerFirstCharAndTrim(resolveMethodPathResult.methodName());

        // 3. 校验 Bean 是否存在
        validateBeanExists(beanName);

        // 4. 根据参数模式进行校验和转换
        boolean useTaskArgs = vo.getTaskArgs() != null && !vo.getTaskArgs().isEmpty();
        if (useTaskArgs) {
            // 新模式: taskArgs — 自动推断参数类型，无需 parameterTypes
            resolveTaskArgs(beanName, methodName, vo);
        } else {
            // 传统模式: params + parameterTypes 兼容旧接口
            validateMethodExists(beanName, methodName, vo.getParameterTypes(), vo.getParams());
        }

        // 5. 去重校验
        checkDuplicateTaskName(vo.getTaskName());

        // 6. 构建实体并保存
        SysScheduledTask scheduledTask = new SysScheduledTask();
        scheduledTask.setTaskName(vo.getTaskName());
        scheduledTask.setCronExpression(vo.getCronExpression());
        scheduledTask.setBeanName(beanName);
        scheduledTask.setMethodName(methodName);
        scheduledTask.setStatus(vo.getStatus() != null ? vo.getStatus() : 1);

        if (useTaskArgs) {
            // 推荐模式: 只存 taskArgs，执行层 invokeWithTaskArgs() 自动推断类型和参数值
            try {
                scheduledTask.setTaskArgs(objectMapper.writeValueAsString(vo.getTaskArgs()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("taskArgs序列化失败: " + e.getMessage(), e);
            }
        } else {
            // 传统模式存储
            scheduledTask.setParameterTypes(teakUtils.resolveReferenceClassName(vo.getParameterTypes()));
            if (vo.getParams() != null && !vo.getParams().isEmpty()) {
                try {
                    scheduledTask.setParams(new ObjectMapper().writeValueAsString(vo.getParams()));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("参数序列化失败: " + e.getMessage(), e);
                }
            }
        }

        sysScheduledTaskMapper.insert(scheduledTask);
        log.info("成功添加定时任务: {} [{}.{}]", vo.getTaskName(), beanName, methodName);

        // 刷新定时任务
        refreshScheduledTasks();
    }

    /**
     * 智能解析 taskArgs — 按位置顺序自动匹配参数并推断类型
     *
     * <p>用户传入的 JSON 值按顺序依次对应方法的第1、第2...个参数，
     * 键名仅作标识用途（不影响匹配），系统自动从方法签名推断目标类型。
     *
     * <p>示例：方法 getDeviceFaultRecords(String startTime, String endTime)
     * <pre>
     * taskArgs: {"startTime":"2022-04-01","endTime":"2024-08-01"}
     *   → 参数0="2022-04-01"(String), 参数1="2024-08-01"(String)
     *
     * taskArgs: {"a":"2022-04-01","b":"2024-08-01"}   ← 键名随意
     *   → 参数0="2022-04-01"(String), 参数1="2024-08-01"(String)
     * </pre>
     */
    private void resolveTaskArgs(String beanName, String methodName, SysScheduledTaskVo vo) {
        try {
            Object bean = applicationContext.getBean(beanName);
            Method targetMethod = findTargetMethod(bean, methodName);

            Parameter[] parameters = targetMethod.getParameters();
            Map<String, Object> taskArgs = vo.getTaskArgs();

            if (parameters.length == 0) {
                if (!taskArgs.isEmpty()) {
                    log.warn("方法 {}.{} 无参数，但提供了 taskArgs，将忽略参数", beanName, methodName);
                }
                vo.getTaskArgs().put("__resolvedTypes", new String[0]);
                return;
            }

            // 按位置顺序取值（JSON的values顺序与插入顺序一致）
            List<Object> orderedParams = new ArrayList<>();
            List<String> resolvedTypeNames = new ArrayList<>();
            List<Object> values = new ArrayList<>(taskArgs.values());

            for (int i = 0; i < parameters.length; i++) {
                Class<?> paramType = parameters[i].getType();
                Object rawValue = values.get(i);

                // 自动类型转换
                Object convertedValue = objectMapper.convertValue(rawValue, paramType);
                orderedParams.add(convertedValue);
                resolvedTypeNames.add(paramType.getName());

                log.info("参数[{}] = {} ({}) → {}", i, rawValue,
                        rawValue.getClass().getSimpleName(), paramType.getSimpleName());
            }

            // 存储推断的类型供执行层使用
            vo.getTaskArgs().put("__resolvedTypes", resolvedTypeNames.toArray(new String[0]));

            log.info("taskArgs 解析完成: {}.{} 参数={}, 类型={}",
                    beanName, methodName, orderedParams, resolvedTypeNames);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("taskArgs 解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 查找目标方法（支持方法重载场景）
     * 同时搜索接口和实现类上的 @Param 注解
     */
    private Method findTargetMethod(Object bean, String methodName) throws NoSuchMethodException {
        // 优先查找无参方法
        try {
            return bean.getClass().getMethod(methodName);
        } catch (NoSuchMethodException ignored) {
        }

        // 列出所有同名方法
        Method[] candidates = Arrays.stream(bean.getClass().getMethods())
                .filter(m -> m.getName().equals(methodName))
                .toArray(Method[]::new);

        if (candidates.length == 0) {
            throw new NoSuchMethodException("方法不存在: " + methodName);
        }

        if (candidates.length == 1) {
            return candidates[0];
        }

        // 多个重载方法时，返回参数最多的那个（通常是最完整的版本）
        Arrays.sort(candidates, Comparator.comparingInt(m -> -m.getParameterCount()));
        log.warn("发现{}个重载的 {} 方法，选择参数最多的版本", candidates.length, methodName);
        return candidates[0];
    }


    /**
     * 解析 methodPath 为 beanName 和 methodName
     * 支持格式: "beanName.methodName"
     */
    private ResolveMethodPathResult resolveMethodPath(SysScheduledTaskVo vo) {
        if (vo.getMethodPath() == null || vo.getMethodPath().isBlank()) {
            return new ResolveMethodPathResult(null, null);
        }
        String path = vo.getMethodPath().trim();
        int dotIndex = path.lastIndexOf('.');
        if (dotIndex <= 0 || dotIndex >= path.length() - 1) {
            throw new IllegalArgumentException("methodPath 格式错误，应为: beanName.methodName，示例: deviceFaultRecordFetchTask.fetchDeviceFaultRecords");
        }
        String beanName = path.substring(0, dotIndex);
        String methodName = path.substring(dotIndex + 1);
        log.info("解析 methodPath: {} -> beanName={}, methodName={}", path, beanName, methodName);
        return new ResolveMethodPathResult(beanName, methodName);
    }

    private record ResolveMethodPathResult(String beanName, String methodName) {
    }

    /**
     * 校验 Bean 是否存在
     */
    private void validateBeanExists(String beanName) {
        if (!applicationContext.containsBeanDefinition(beanName)) {
            // 尝试按接口类型查找
            String[] candidateBeans = applicationContext.getBeanNamesForType(Object.class);
            StringBuilder similar = new StringBuilder();
            for (String name : candidateBeans) {
                if (name.toLowerCase().contains(beanName.toLowerCase()) ||
                        beanName.toLowerCase().contains(name.toLowerCase())) {
                    similar.append(name).append(", ");
                }
            }
            String hint = similar.length() > 0
                    ? "相似的Bean名称: " + similar.substring(0, similar.length() - 2)
                    : "提示: Bean名称为类名首字母小写(如 DeviceFaultRecordFetchTask -> deviceFaultRecordFetchTask)";
            throw new IllegalArgumentException("Bean不存在: [" + beanName + "]。 " + hint);
        }
    }

    /**
     * 校验方法是否存在且参数匹配
     */
    private void validateMethodExists(String beanName, String methodName, String parameterTypes, List<?> params) {
        try {
            Object bean = applicationContext.getBean(beanName);
            Class<?>[] paramClasses = null;
            if (parameterTypes != null && !parameterTypes.isBlank()) {
                String[] typeNames = parameterTypes.split(",");
                paramClasses = new Class[typeNames.length];
                for (int i = 0; i < typeNames.length; i++) {
                    Class<? extends Serializable> aClass = teakUtils.resolveClassName(typeNames[i].trim());
                    if (aClass != null) {
                        paramClasses[i] = aClass;
                    } else {
                        paramClasses[i] = Class.forName(typeNames[i].trim());
                    }
                }
            } else if (params != null && !params.isEmpty()) {
                // 自动推断参数类型
                paramClasses = new Class[params.size()];
                for (int i = 0; i < params.size(); i++) {
                    paramClasses[i] = params.get(i).getClass();
                }
            }

            Method method;
            if (paramClasses != null && paramClasses.length > 0) {
                method = bean.getClass().getMethod(methodName, paramClasses);
            } else {
                method = bean.getClass().getMethod(methodName);
            }
            log.info("方法验证通过: {}.{}", beanName, method.getName());
        } catch (NoSuchMethodException e) {
            // 列出可用方法供参考
            Object bean = applicationContext.getBean(beanName);
            StringBuilder availableMethods = new StringBuilder();
            for (Method m : bean.getClass().getMethods()) {
                if (m.getName().equals(methodName) ||
                        m.getDeclaringClass() == bean.getClass() && m.getParameterCount() <= 4) {
                    availableMethods.append(m.getName()).append("(");
                    for (Class<?> p : m.getParameterTypes()) {
                        availableMethods.append(p.getSimpleName()).append(",");
                    }
                    if (m.getParameterCount() > 0) {
                        availableMethods.setLength(availableMethods.length() - 1);
                    }
                    availableMethods.append("), ");
                }
            }
            throw new IllegalArgumentException(
                    "方法不存在或参数不匹配: [" + beanName + "." + methodName + "]。可用方法: " +
                            (availableMethods.length() > 0 ? availableMethods.substring(0, Math.min(availableMethods.length() - 2, 300)) : "无"));
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("参数类型不存在: " + e.getMessage());
        }
    }

    /**
     * 检查任务名是否重复
     */
    private void checkDuplicateTaskName(String taskName) {
        SysScheduledTask existing = sysScheduledTaskMapper.selectByTaskName(taskName);
        if (existing != null) {
            throw new IllegalArgumentException("任务名已存在: [" + taskName + "]，请使用不同的名称或先删除已有任务");
        }
    }

    @Override
    public void refreshScheduledTasks() {
        applicationContext.publishEvent(new TaskRefreshEvent(this));
        log.info("已发布任务刷新事件");
    }
}