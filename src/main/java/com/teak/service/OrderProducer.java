package com.teak.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teak.model.Order;
import com.teak.system.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Created with: IntelliJ IDEA
 *
 * @Author: li zheng
 * @Date: 2025/5/23 09:38
 * @Project: teakWeb2
 * @File: OrderProducer.java
 * @Description:
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class OrderProducer {
    private final RabbitTemplate rabbitTemplate;

    public void sendOrder(Order order) {
        try {
            CorrelationData correlationId = new CorrelationData(UUID.randomUUID().toString());
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ORDER_EXCHANGE,
                    RabbitMQConfig.ORDER_ROUTING_KEY,
                    order,
                    message -> {
                        message.getMessageProperties().setHeader("orderType", "VIP");
                        return message;
                    },
                    correlationId
            );
            log.info("发送订单消息：{}", new ObjectMapper().writeValueAsString(order));
        } catch (AmqpException e) {
            log.error("消息发送失败", e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
