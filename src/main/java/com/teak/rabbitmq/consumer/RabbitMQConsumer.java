package com.teak.rabbitmq.consumer;

import com.rabbitmq.client.Channel;
import com.teak.rabbitmq.config.RabbitMQConfig;
import com.teak.service.DeviceFaultRecordsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static cn.hutool.core.thread.ThreadUtil.sleep;

/**
 * RabbitMQ 消费者（手动确认模式）
 * <p>
 * 为什么必须用手动确认？
 * 1. 死信队列需要调用 basicReject()/basicNack()
 * 2. 自动确认模式下无法控制消息是否进入死信队列
 * 3. 只有手动确认才能保证消息不丢失
 */
@Slf4j
@Component
@Lazy(false)  // 禁用延迟初始化，确保消费者在应用启动时立即注册
public class RabbitMQConsumer {

    private final DeviceFaultRecordsService deviceFaultRecordsService;

    public RabbitMQConsumer(DeviceFaultRecordsService deviceFaultRecordsService) {
        this.deviceFaultRecordsService = deviceFaultRecordsService;
    }

    // ==================== 1. Hello World 简单模式 ====================

    /**
     * 接收简单消息 - 手动确认
     */
    @RabbitListener(queues = RabbitMQConfig.HELLO_QUEUE, containerFactory = "highPrefetchFactory", concurrency = "10")
    public void receiveHelloMessage(String message, Channel channel,
                                    @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            log.info("【Hello World模式】接收到消息: {}", message);

            // 业务处理逻辑
            processMessage(message);

            // 手动确认消息（告诉RabbitMQ消息已成功处理）
            channel.basicAck(deliveryTag, false);
            log.info("【Hello World模式】消息已确认");

        } catch (Exception e) {
            log.error("【Hello World模式】处理失败: {}", message, e);
            try {
                // 拒绝消息且不重新入队 → 消息进入死信队列
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ex) {
                log.error("拒绝消息失败", ex);
            }
        }
    }

    // ==================== 2. Work Queues 工作队列模式 ====================

    /**
     * 工作队列消费者1 - 手动确认
     */
    @RabbitListener(queues = RabbitMQConfig.WORK_QUEUE)
    public void receiveWorkMessage1(String message, Channel channel,
                                    @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            log.info("【Work Queues模式 - 消费者1】接收到消息: {}", message);
            TimeUnit.SECONDS.sleep(1);  // 模拟处理耗时

            channel.basicAck(deliveryTag, false);
            log.info("【Work Queues模式 - 消费者1】处理完成并确认");

        } catch (Exception e) {
            log.error("【Work Queues模式 - 消费者1】处理失败", e);
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ex) {
                log.error("拒绝消息失败", ex);
            }
        }
    }

    /**
     * 工作队列消费者2 - 手动确认
     */
    @RabbitListener(queues = RabbitMQConfig.WORK_QUEUE)
    public void receiveWorkMessage2(String message, Channel channel,
                                    @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            log.info("【Work Queues模式 - 消费者2】接收到消息: {}", message);
            TimeUnit.SECONDS.sleep(2);  // 模拟处理耗时

            channel.basicAck(deliveryTag, false);
            log.info("【Work Queues模式 - 消费者2】处理完成并确认");

        } catch (Exception e) {
            log.error("【Work Queues模式 - 消费者2】处理失败", e);
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ex) {
                log.error("拒绝消息失败", ex);
            }
        }
    }

    // ==================== 3. Publish/Subscribe 发布订阅模式 ====================

    /**
     * 发布订阅消费者1 - 手动确认
     */
    @RabbitListener(queues = RabbitMQConfig.FANOUT_QUEUE_1)
    public void receiveFanoutMessage1(String message, Channel channel,
                                      @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            log.info("【Publish/Subscribe模式 - 消费者1】接收到消息: {}", message);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("【Publish/Subscribe模式 - 消费者1】处理失败", e);
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ex) {
                log.error("拒绝消息失败", ex);
            }
        }
    }

    /**
     * 发布订阅消费者2 - 手动确认
     */
    @RabbitListener(queues = RabbitMQConfig.FANOUT_QUEUE_2)
    public void receiveFanoutMessage2(String message, Channel channel,
                                      @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            log.info("【Publish/Subscribe模式 - 消费者2】接收到消息: {}", message);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("【Publish/Subscribe模式 - 消费者2】处理失败", e);
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ex) {
                log.error("拒绝消息失败", ex);
            }
        }
    }

    // ==================== 4. Routing 路由模式 ====================

    /**
     * 路由消费者 - info队列 - 手动确认
     */
    @RabbitListener(queues = RabbitMQConfig.DIRECT_QUEUE_INFO)
    public void receiveDirectInfoMessage(String message, Channel channel,
                                         @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            log.info("【Routing模式 - Info队列】接收到消息: {}", message);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("【Routing模式 - Info队列】处理失败", e);
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ex) {
                log.error("拒绝消息失败", ex);
            }
        }
    }

    /**
     * 路由消费者 - error队列 - 手动确认
     */
    @RabbitListener(queues = RabbitMQConfig.DIRECT_QUEUE_ERROR)
    public void receiveDirectErrorMessage(String message, Channel channel,
                                          @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            log.info("【Routing模式 - Error队列】接收到消息: {}", message);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("【Routing模式 - Error队列】处理失败", e);
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ex) {
                log.error("拒绝消息失败", ex);
            }
        }
    }

    // ==================== 5. Topics 主题模式 ====================

    /**
     * 主题消费者1 - 匹配 user.* - 手动确认
     */
    @RabbitListener(queues = RabbitMQConfig.TOPIC_QUEUE_1)
    public void receiveTopicMessage1(String message, Channel channel,
                                     @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            log.info("【Topics模式 - 队列1(user.*)】接收到消息: {}", message);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("【Topics模式 - 队列1】处理失败", e);
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ex) {
                log.error("拒绝消息失败", ex);
            }
        }
    }

    /**
     * 主题消费者2 - 匹配 # - 手动确认
     */
    @RabbitListener(queues = RabbitMQConfig.TOPIC_QUEUE_2)
    public void receiveTopicMessage2(String message, Channel channel,
                                     @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            log.info("【Topics模式 - 队列2(#)】接收到消息: {}", message);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("【Topics模式 - 队列2】处理失败", e);
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ex) {
                log.error("拒绝消息失败", ex);
            }
        }
    }

    // ==================== 6. RPC 模式 ====================

    /**
     * RPC 服务端（接收请求并返回响应）
     * 注意：RPC模式Spring会自动处理确认，不需要手动确认
     */
    @RabbitListener(queues = RabbitMQConfig.RPC_QUEUE)
    public String receiveRPCMessage(String message) {
        log.info("【RPC模式】接收到请求: {}", message);

        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String response = "RPC Response: " + message.toUpperCase();
        log.info("【RPC模式】返回响应: {}", response);
        return response;
    }

    // ==================== 7. 死信队列模式 ====================

    /**
     * 普通队列消费者 - 演示死信产生
     * <p>
     * 死信产生方式：
     * 1. 消息被拒绝（basicReject/basicNack）且不重新入队
     * 2. 消息过期（TTL）
     * 3. 队列达到最大长度
     */
    @RabbitListener(queues = RabbitMQConfig.NORMAL_QUEUE)
    public void receiveNormalMessage(String message, Channel channel,
                                     @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            log.info("【死信队列模式 - 普通队列】接收到消息: {}", message);

            // 演示：拒绝以"REJECT:"开头的消息，使其进入死信队列
            if (message.startsWith("REJECT:")) {
                // 拒绝消息且不重新入队 → 消息进入死信队列
                channel.basicReject(deliveryTag, false);
                log.warn("【死信队列模式】消息被拒绝，将进入死信队列: {}", message);
            } else {
                // 正常处理并确认
                processMessage(message);
                channel.basicAck(deliveryTag, false);
                log.info("【死信队列模式】消息处理成功并已确认: {}", message);
            }

        } catch (Exception e) {
            log.error("【死信队列模式】处理失败", e);
            try {
                // 处理失败，拒绝且不重新入队 → 进入死信队列
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ex) {
                log.error("拒绝消息失败", ex);
            }
        }
    }

    /**
     * 死信队列消费者 - 处理死信消息
     * <p>
     * 死信队列的作用：
     * 1. 保存无法被正常消费的消息
     * 2. 提供补救机会（人工介入、重试等）
     */
    @RabbitListener(queues = RabbitMQConfig.DEAD_LETTER_QUEUE)
    public void receiveDeadLetterMessage(String message, Message amqpMessage,
                                         Channel channel,
                                         @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            log.error("【死信队列模式 - 死信队列】接收到死信消息: {}", message);

            // 打印死信原因
            Object reason = amqpMessage.getMessageProperties().getHeaders().get("x-first-death-reason");
            if (reason != null) {
                log.error("【死信队列模式】死信原因: {}", reason);
            }

            // 死信消息处理策略：
            // 1. 记录到数据库，便于后续分析
            // 2. 发送告警通知（邮件、短信、钉钉等）
            // 3. 人工介入处理
            // 4. 尝试重新处理（谨慎使用，避免死循环）

            log.info("【死信队列模式】死信消息已处理");

            // 处理完成后确认消息
            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("【死信队列模式】处理死信消息失败", e);
            try {
                // 处理失败，拒绝且不重新入队（避免死循环）
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ex) {
                log.error("拒绝消息失败", ex);
            }
        }
    }

    /**
     * 模拟消息处理
     */
    private void processMessage(String message) {
        // 模拟业务逻辑
//        deviceFaultRecordsService.getDeviceFaultRecords("2024-09-01 00:00:00", "2024-12-30 00:00:00");
        log.info("处理消息: {}", message);
    }
}
