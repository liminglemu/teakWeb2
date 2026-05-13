package com.teak.rabbitmq.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ 配置类
 * 定义各种模式的交换机、队列和绑定关系
 */
@Configuration
@EnableRabbit  // 启用RabbitMQ监听器支持
public class RabbitMQConfig {

    // ==================== 1. Hello World 简单模式 ====================
    public static final String HELLO_QUEUE = "hello.queue";

    // ==================== 2. Work Queues 工作队列模式 ====================
    public static final String WORK_QUEUE = "work.queue";

    // ==================== 3. Publish/Subscribe 发布订阅模式 ====================
    public static final String FANOUT_EXCHANGE = "fanout.exchange";
    public static final String FANOUT_QUEUE_1 = "fanout.queue.1";
    public static final String FANOUT_QUEUE_2 = "fanout.queue.2";

    // ==================== 4. Routing 路由模式 ====================
    public static final String DIRECT_EXCHANGE = "direct.exchange";
    public static final String DIRECT_QUEUE_INFO = "direct.queue.info";
    public static final String DIRECT_QUEUE_ERROR = "direct.queue.error";
    public static final String ROUTING_KEY_INFO = "info";
    public static final String ROUTING_KEY_ERROR = "error";
    public static final String ROUTING_KEY_WARNING = "warning";

    // ==================== 5. Topics 主题模式 ====================
    public static final String TOPIC_EXCHANGE = "topic.exchange";
    public static final String TOPIC_QUEUE_1 = "topic.queue.1";
    public static final String TOPIC_QUEUE_2 = "topic.queue.2";
    public static final String TOPIC_ROUTING_KEY_1 = "user.add";
    public static final String TOPIC_ROUTING_KEY_2 = "user.delete";
    public static final String TOPIC_ROUTING_KEY_3 = "order.create";

    // ==================== 6. RPC 模式 ====================
    public static final String RPC_QUEUE = "rpc.queue";
    public static final String RPC_EXCHANGE = "rpc.exchange";
    public static final String RPC_ROUTING_KEY = "rpc.key";

    // ==================== 7. 死信队列模式 ====================
    public static final String NORMAL_EXCHANGE = "normal.exchange";
    public static final String NORMAL_QUEUE = "normal.queue";
    public static final String NORMAL_ROUTING_KEY = "normal.key";
    
    public static final String DEAD_LETTER_EXCHANGE = "dead.letter.exchange";
    public static final String DEAD_LETTER_QUEUE = "dead.letter.queue";
    public static final String DEAD_LETTER_ROUTING_KEY = "dead.letter.key";

    /**
     * 消息转换器 - 使用JSON格式
     */
    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 配置RabbitTemplate使用JSON转换器
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        
        // 设置Mandatory，确保消息投递失败时返回
        rabbitTemplate.setMandatory(true);
        
        return rabbitTemplate;
    }

    // ==================== 1. Hello World 简单模式 ====================
    
    @Bean
    public Queue helloQueue() {
        return QueueBuilder.durable(HELLO_QUEUE).build();
    }

    // ==================== 2. Work Queues 工作队列模式 ====================
    
    @Bean
    public Queue workQueue() {
        return QueueBuilder.durable(WORK_QUEUE)
                .withArgument("x-message-ttl", 10000) // 消息过期时间10秒
                .build();
    }

    // ==================== 3. Publish/Subscribe 发布订阅模式 ====================
    
    @Bean
    public FanoutExchange fanoutExchange() {
        return ExchangeBuilder.fanoutExchange(FANOUT_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue fanoutQueue1() {
        return QueueBuilder.durable(FANOUT_QUEUE_1).build();
    }

    @Bean
    public Queue fanoutQueue2() {
        return QueueBuilder.durable(FANOUT_QUEUE_2).build();
    }

    @Bean
    public Binding fanoutBinding1() {
        return BindingBuilder.bind(fanoutQueue1()).to(fanoutExchange());
    }

    @Bean
    public Binding fanoutBinding2() {
        return BindingBuilder.bind(fanoutQueue2()).to(fanoutExchange());
    }

    // ==================== 4. Routing 路由模式 ====================
    
    @Bean
    public DirectExchange directExchange() {
        return ExchangeBuilder.directExchange(DIRECT_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue directQueueInfo() {
        return QueueBuilder.durable(DIRECT_QUEUE_INFO).build();
    }

    @Bean
    public Queue directQueueError() {
        return QueueBuilder.durable(DIRECT_QUEUE_ERROR).build();
    }

    @Bean
    public Binding directBindingInfo() {
        return BindingBuilder.bind(directQueueInfo()).to(directExchange()).with(ROUTING_KEY_INFO);
    }

    @Bean
    public Binding directBindingError() {
        return BindingBuilder.bind(directQueueError()).to(directExchange()).with(ROUTING_KEY_ERROR);
    }

    @Bean
    public Binding directBindingWarning() {
        return BindingBuilder.bind(directQueueError()).to(directExchange()).with(ROUTING_KEY_WARNING);
    }

    // ==================== 5. Topics 主题模式 ====================
    
    @Bean
    public TopicExchange topicExchange() {
        return ExchangeBuilder.topicExchange(TOPIC_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue topicQueue1() {
        return QueueBuilder.durable(TOPIC_QUEUE_1).build();
    }

    @Bean
    public Queue topicQueue2() {
        return QueueBuilder.durable(TOPIC_QUEUE_2).build();
    }

    @Bean
    public Binding topicBinding1() {
        // user.* 匹配 user.add, user.delete 等
        return BindingBuilder.bind(topicQueue1()).to(topicExchange()).with("user.*");
    }

    @Bean
    public Binding topicBinding2() {
        // # 匹配一个或多个词
        return BindingBuilder.bind(topicQueue2()).to(topicExchange()).with("#");
    }

    // ==================== 6. RPC 模式 ====================
    
    @Bean
    public Queue rpcQueue() {
        return QueueBuilder.durable(RPC_QUEUE).build();
    }

    @Bean
    public DirectExchange rpcExchange() {
        return ExchangeBuilder.directExchange(RPC_EXCHANGE).durable(true).build();
    }

    @Bean
    public Binding rpcBinding() {
        return BindingBuilder.bind(rpcQueue()).to(rpcExchange()).with(RPC_ROUTING_KEY);
    }

    // ==================== 7. 死信队列模式 ====================
    
    @Bean
    public Queue normalQueue() {
        Map<String, Object> args = new HashMap<>();
        // 设置死信交换机
        args.put("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE);
        // 设置死信路由键
        args.put("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY);
        // 设置队列消息过期时间（毫秒）
        args.put("x-message-ttl", 10000);
        // 设置队列最大长度
        // args.put("x-max-length", 10);
        
        return QueueBuilder.durable(NORMAL_QUEUE).withArguments(args).build();
    }

    @Bean
    public DirectExchange normalExchange() {
        return ExchangeBuilder.directExchange(NORMAL_EXCHANGE).durable(true).build();
    }

    @Bean
    public Binding normalBinding() {
        return BindingBuilder.bind(normalQueue()).to(normalExchange()).with(NORMAL_ROUTING_KEY);
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DEAD_LETTER_QUEUE).build();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder.directExchange(DEAD_LETTER_EXCHANGE).durable(true).build();
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with(DEAD_LETTER_ROUTING_KEY);
    }

    /**
     * 低并发容器工厂 - prefetch=1（适用于处理慢、需严格有序的场景）
     */
    @Bean(name = "lowPrefetchFactory")
    public SimpleRabbitListenerContainerFactory lowPrefetchFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setPrefetchCount(1);
        return factory;
    }

    /**
     * 高并发容器工厂 - prefetch=10（适用于处理快、高吞吐场景）
     */
    @Bean(name = "highPrefetchFactory")
    public SimpleRabbitListenerContainerFactory highPrefetchFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setPrefetchCount(10);
        return factory;
    }
}
