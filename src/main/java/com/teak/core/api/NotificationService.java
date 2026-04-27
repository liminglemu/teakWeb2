package com.teak.core.api;

/**
 * 通知服务接口，用于抽象不同的通知渠道（邮件、短信、钉钉等）。
 */
public interface NotificationService {

    /**
     * 发送文本消息
     */
    void sendText(String recipient, String content);

    /**
     * 发送HTML消息
     */
    void sendHtml(String recipient, String htmlContent);

    /**
     * 发送带主题的消息（如邮件）
     */
    void sendWithSubject(String recipient, String subject, String content);
}