package com.teak.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.teak.model.Order;
import com.teak.system.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Created with: IntelliJ IDEA
 *
 * @Author: li zheng
 * @Date: 2025/5/23 10:00
 * @Project: teakWeb2
 * @File: OrderConsumer.java
 * @Description:
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class OrderConsumer {
    @RabbitListener(queues = RabbitMQConfig.ORDER_QUEUE)
    public void processOrder(Message message, Channel channel) {
        try {
            Order order = new ObjectMapper().convertValue(message.getBody(), Order.class);
            log.info("处理订单：{}", order);

            // 业务处理逻辑
            // ...

            // 手动确认消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("订单处理失败", e);
            try {
                // 拒绝消息并重新入队
                channel.basicReject(
                        message.getMessageProperties().getDeliveryTag(),
                        true
                );
            } catch (IOException ex) {
                log.error("消息拒绝失败", ex);
            }
        }
    }

    @RabbitListener(queues = "dlx-queue")
    public void processDeadLetter(Message message) {
        log.error("处理死信消息：{}", new String(message.getBody()));
    }
}
