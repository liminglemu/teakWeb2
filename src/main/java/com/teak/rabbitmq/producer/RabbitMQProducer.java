package com.teak.rabbitmq.producer;

import com.teak.rabbitmq.config.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * RabbitMQ 生产者
 * 演示各种消息模式的消息发送
 */
@Slf4j
@Component
public class RabbitMQProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    // ==================== 1. Hello World 简单模式 ====================
    
    /**
     * 发送简单消息（Hello World模式）
     */
    public void sendHelloMessage(String message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.HELLO_QUEUE, message);
        log.info("【Hello World模式】发送消息: {}", message);
    }

    // ==================== 2. Work Queues 工作队列模式 ====================
    
    /**
     * 发送工作队列消息
     */
    public void sendWorkMessage(String message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.WORK_QUEUE, message);
        log.info("【Work Queues模式】发送消息: {}", message);
    }

    // ==================== 3. Publish/Subscribe 发布订阅模式 ====================
    
    /**
     * 发送发布订阅消息（Fanout Exchange会广播到所有绑定队列）
     */
    public void sendFanoutMessage(String message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.FANOUT_EXCHANGE, "", message);
        log.info("【Publish/Subscribe模式】发送消息: {}", message);
    }

    // ==================== 4. Routing 路由模式 ====================
    
    /**
     * 发送路由消息 - info级别
     */
    public void sendDirectInfoMessage(String message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.DIRECT_EXCHANGE, 
                RabbitMQConfig.ROUTING_KEY_INFO, message);
        log.info("【Routing模式】发送INFO消息: {}", message);
    }

    /**
     * 发送路由消息 - error级别
     */
    public void sendDirectErrorMessage(String message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.DIRECT_EXCHANGE, 
                RabbitMQConfig.ROUTING_KEY_ERROR, message);
        log.info("【Routing模式】发送ERROR消息: {}", message);
    }

    /**
     * 发送路由消息 - warning级别（会路由到error队列）
     */
    public void sendDirectWarningMessage(String message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.DIRECT_EXCHANGE, 
                RabbitMQConfig.ROUTING_KEY_WARNING, message);
        log.info("【Routing模式】发送WARNING消息: {}", message);
    }

    // ==================== 5. Topics 主题模式 ====================
    
    /**
     * 发送主题消息 - user.add
     */
    public void sendTopicUserAddMessage(String message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.TOPIC_EXCHANGE, 
                RabbitMQConfig.TOPIC_ROUTING_KEY_1, message);
        log.info("【Topics模式】发送 user.add 消息: {}", message);
    }

    /**
     * 发送主题消息 - user.delete
     */
    public void sendTopicUserDeleteMessage(String message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.TOPIC_EXCHANGE, 
                "user.delete", message);
        log.info("【Topics模式】发送 user.delete 消息: {}", message);
    }

    /**
     * 发送主题消息 - order.create
     */
    public void sendTopicOrderCreateMessage(String message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.TOPIC_EXCHANGE, 
                RabbitMQConfig.TOPIC_ROUTING_KEY_3, message);
        log.info("【Topics模式】发送 order.create 消息: {}", message);
    }

    // ==================== 6. RPC 模式 ====================
    
    /**
     * 发送RPC消息并等待响应
     */
    public String sendRPCMessage(String message) {
        log.info("【RPC模式】发送消息: {}", message);
        
        Object response = rabbitTemplate.convertSendAndReceive(
                RabbitMQConfig.RPC_EXCHANGE,
                RabbitMQConfig.RPC_ROUTING_KEY,
                message
        );
        
        String result = response != null ? response.toString() : "No response";
        log.info("【RPC模式】收到响应: {}", result);
        return result;
    }

    // ==================== 7. 死信队列模式 ====================
    
    /**
     * 发送消息到普通队列（会过期进入死信队列）
     */
    public void sendNormalMessage(String message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.NORMAL_EXCHANGE, 
                RabbitMQConfig.NORMAL_ROUTING_KEY, message);
        log.info("【死信队列模式】发送消息到普通队列: {}", message);
    }

    /**
     * 发送消息并设置消息级别TTL（过期时间）
     */
    public void sendMessageWithTTL(String message, int ttl) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.NORMAL_EXCHANGE, 
                RabbitMQConfig.NORMAL_ROUTING_KEY, message, m -> {
            m.getMessageProperties().setExpiration(String.valueOf(ttl));
            return m;
        });
        log.info("【死信队列模式】发送消息（TTL={}ms）: {}", ttl, message);
    }

    /**
     * 发送无法被消费的消息（用于测试拒绝/否定确认）
     */
    public void sendRejectMessage(String message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.NORMAL_EXCHANGE, 
                RabbitMQConfig.NORMAL_ROUTING_KEY, "REJECT:" + message);
        log.info("【死信队列模式】发送将被拒绝的消息: {}", message);
    }
}
