package com.teak.system.utils;

import com.teak.system.exception.BusinessException;
import com.teak.core.api.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.beans.FeatureDescriptor;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Created with: IntelliJ IDEA
 *
 * @Author:
 * @Date: 2023/11/16 10:41
 * @Project: crm
 * @File: TeakUtils.java
 * @Description: 封装工具
 */
@Component
@Slf4j
public class TeakUtils {

    /*private static MimeMessageHelper getMimeMessageHelper(ReceiverEntity receiverEntity, MimeMessage mimeMessage) throws MessagingException {
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        mimeMessageHelper.setFrom(receiverEntity.getSenderEmail());
        mimeMessageHelper.setTo(receiverEntity.getReceiver());
        mimeMessageHelper.setSubject(receiverEntity.getMessageSubject());
        if (receiverEntity.getMessageContent() == null) {
            throw new GlobalException("文本内容为空，无法进行发送邮件");
        }
        mimeMessageHelper.setText(receiverEntity.getMessageContent(), true);
        mimeMessageHelper.setSentDate(new Date());
        return mimeMessageHelper;
    }

    public void emailHandler(ReceiverEntity receiverEntity, MimeMessage mimeMessage) {
        try {
            MimeMessageHelper mimeMessageHelper = getMimeMessageHelper(receiverEntity, mimeMessage);
            if (receiverEntity.getFiles() != null && receiverEntity.getFiles().length > 0) {
                Arrays.stream(receiverEntity.getFiles()).forEach(file -> {
                    try {
                        mimeMessageHelper.addAttachment(file.getName(), file);
                    } catch (MessagingException e) {
                        throw new GlobalException("文件无法识别，请联系管理人员");
                    }
                });
            } else if (receiverEntity.getOutputStream() != null) {
                ByteArrayOutputStream outputStream = receiverEntity.getOutputStream();
                ByteArrayResource byteArrayResource = new ByteArrayResource(outputStream.toByteArray());
                mimeMessageHelper.addAttachment(receiverEntity.getMessageSubject() + ".docx", byteArrayResource);
            }
        } catch (MessagingException e) {
            throw new GlobalException(EmailResultEnum.FAIL.getMessage());
        }
    }*/

    /**
     * 首字母小写并去除空格
     *
     * @param str 待处理的字符串
     * @return 返回字符串
     */
    public String lowerFirstCharAndTrim(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        char firstChar = Character.toLowerCase(str.charAt(0));
        String newString = (str.length() > 1) ? firstChar + str.substring(1) : String.valueOf(firstChar);
        return newString.trim();
    }


    /**
     * 解析引用类型和数组类型
     *
     * @param typeName 类型名称
     * @return 返回用逗号分割的引用类型和数组类型全类名
     */
    public String resolveReferenceClassName(String typeName) {
        if (typeName == null) {
            return null;
        }

        ArrayList<String> typeClassNames = new ArrayList<>();
        String[] split = typeName.split(",");
        for (String splitName : split) {
            try {
                boolean handled = false;

                // 处理引用类型
                Class<? extends Serializable> aClass = getAClass(splitName);
                if (aClass != null) {
                    typeClassNames.add(aClass.getName());
                    handled = true;
                }
                if ("Object".equals(splitName)) {
                    typeClassNames.add(Object.class.getName());
                    handled = true;
                }

                // 处理数组类型
                if (!handled && splitName.endsWith("[]")) {
                    String elementType = splitName.replace("[]", "");
                    Class<? extends Serializable> arrayAClass = getAClass(elementType);
                    if (arrayAClass != null) {
                        typeClassNames.add(Class.forName("[L" + arrayAClass.getName() + ";").getName());
                        handled = true;
                    }
                }

                // 基本数据类型
                if (!handled) {
                    typeClassNames.add(splitName);
                }
            } catch (ClassNotFoundException e) {
                log.error("无法解析类型: {}", splitName, e);
                throw new IllegalArgumentException("无效类型: " + splitName);
            }
        }
        return String.join(",", typeClassNames);
    }

    private static Class<? extends Serializable> getAClass(String splitName) {
        return switch (splitName) {
            case "Integer" -> Integer.class;
            case "Long" -> Long.class;
            case "String" -> String.class;
            case "Double" -> Double.class;
            case "Float" -> Float.class;
            case "BigDecimal" -> BigDecimal.class;
            case "Date" -> Date.class;
            case "Boolean" -> Boolean.class;
            case "Byte" -> Byte.class;
            case "Short" -> Short.class;
            case "Character" -> Character.class;
            default -> null;
        };
    }

    /**
     * 解析基本数据类型
     *
     * @param typeName 类型名称
     * @return 直接返回基本数据类型class类
     */
    public Class<? extends Serializable> resolveClassName(String typeName) {
        return switch (typeName) {
            case "int" -> int.class;
            case "long" -> long.class;
            case "double" -> double.class;
            case "boolean" -> boolean.class;
            case "char" -> char.class;
            case "float" -> float.class;
            case "byte" -> byte.class;
            case "short" -> short.class;
            default -> null;
        };
    }
}
