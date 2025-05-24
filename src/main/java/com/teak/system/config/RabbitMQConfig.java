package com.teak.system.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with: IntelliJ IDEA
 *
 * @Author: li zheng
 * @Date: 2025/5/22 18:28
 * @Project: teakWeb2
 * @File: RabbitMQConfig.java
 * @Description:
 */
@Configuration
public class RabbitMQConfig {
    public static final String ORDER_EXCHANGE = "order-exchange";
    public static final String ORDER_QUEUE = "order-queue";
    public static final String ORDER_ROUTING_KEY = "order.create";

    // 声明直连交换机
    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(ORDER_EXCHANGE, true, false);
    }

    // 声明持久化队列

    /**
     * 👉 这段代码的作用是：
     * 当 order-queue 中的消息变成死信时：
     * RabbitMQ 会将这条消息发送到名为 "dlx-exchange" 的交换机。
     * 使用路由键 "dlx.routing.key" 来决定消息如何路由。
     * ⚠️ 注意：这只是“转发”的第一步，还没有指定消息最终去哪。
     * order-queue（主队列）
     *    ↓（消息变死信）
     *    ↓（通过 x-dead-letter-exchange 和 x-dead-letter-routing-key）
     * dlx-exchange（死信交换机）
     *    ↓（根据绑定关系 Binding）
     *    ↓（路由键 dlx.routing.key）
     * dlx-queue（死信队列）
     */
    @Bean
    public Queue orderQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", "dlx-exchange"); // 死信交换机
        args.put("x-dead-letter-routing-key", "dlx.routing.key");
        args.put("x-message-ttl", 60000);//60秒过期
        return QueueBuilder.durable(ORDER_QUEUE).withArguments(args).build();
    }

    // 绑定队列和交换机
    @Bean
    public Binding orderBinding() {
        return BindingBuilder.bind(orderQueue())
                .to(orderExchange())
                .with(ORDER_ROUTING_KEY);
    }

    // 死信队列配置
    @Bean
    public Queue deadLetterQueue() {
        return new Queue("dlx-queue", true);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange("dlx-exchange", true, false);
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with("dlx.routing.key");
    }
}
