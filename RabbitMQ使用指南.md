# RabbitMQ 使用指南

## 目录
1. [项目集成说明](#项目集成说明)
2. [RabbitMQ六种工作模式](#rabbitmq六种工作模式)
3. [死信队列详解](#死信队列详解)
4. [测试接口说明](#测试接口说明)
5. [Docker启动RabbitMQ](#docker启动rabbitmq)

---

## 项目集成说明

### 已添加的依赖
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```

### 配置说明
已在 `application-dev.yaml` 中配置RabbitMQ连接信息：
```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    virtual-host: /
```

---

## RabbitMQ六种工作模式

### 1. Hello World（简单模式）
**特点**：一个生产者，一个消费者，一对一通信。

**应用场景**：简单的任务分发，不需要复杂路由。

**代码示例**：
```java
// 发送消息
rabbitTemplate.convertAndSend("hello.queue", message);

// 接收消息
@RabbitListener(queues = "hello.queue")
public void receive(String message) {
    log.info("接收到消息: {}", message);
}
```

---

### 2. Work Queues（工作队列模式）
**特点**：一个队列，多个消费者竞争消费，默认轮询分发。

**应用场景**：任务较重，需要多个消费者并行处理。

**关键配置**：
- `spring.rabbitmq.listener.simple.prefetch=1`：限制每次只发送一条消息，实现能者多劳。

**代码示例**：
```java
// 两个消费者监听同一个队列
@RabbitListener(queues = "work.queue")
public void consumer1(String message) { }

@RabbitListener(queues = "work.queue")
public void consumer2(String message) { }
```

---

### 3. Publish/Subscribe（发布订阅模式）
**特点**：使用Fanout交换机，广播消息到所有绑定队列。

**应用场景**：消息需要被多个系统同时消费，如日志广播。

**代码示例**：
```java
// 发送消息（不需要路由键）
rabbitTemplate.convertAndSend("fanout.exchange", "", message);

// 交换机配置
@Bean
public FanoutExchange fanoutExchange() {
    return new FanoutExchange("fanout.exchange");
}
```

---

### 4. Routing（路由模式）
**特点**：使用Direct交换机，根据路由键精确匹配队列。

**应用场景**：不同类型的消息需要被不同的队列处理，如日志分级处理。

**代码示例**：
```java
// 发送消息（指定路由键）
rabbitTemplate.convertAndSend("direct.exchange", "info", message);
rabbitTemplate.convertAndSend("direct.exchange", "error", message);

// 绑定配置
@Bean
public Binding bindingInfo() {
    return BindingBuilder.bind(infoQueue()).to(directExchange()).with("info");
}
```

---

### 5. Topics（主题模式）
**特点**：使用Topic交换机，根据路由键模式匹配（支持通配符）。

**通配符**：
- `*`：匹配一个词
- `#`：匹配一个或多个词

**应用场景**：复杂的消息路由，如根据业务模块+操作类型路由。

**代码示例**：
```java
// 路由键示例
"user.add"    -> 匹配 "user.*"
"order.create" -> 匹配 "order.*"
"user.order.pay" -> 匹配 "user.#"

// 发送消息
rabbitTemplate.convertAndSend("topic.exchange", "user.add", message);
```

---

### 6. RPC（远程调用模式）
**特点**：客户端发送消息后等待响应，实现同步调用。

**应用场景**：需要立即获取处理结果的场景。

**代码示例**：
```java
// 客户端发送并接收响应
Object response = rabbitTemplate.convertSendAndReceive(
    "rpc.exchange", "rpc.key", message);

// 服务端处理并返回
@RabbitListener(queues = "rpc.queue")
public String handleRPC(String message) {
    return "Response: " + message;
}
```

---

## 死信队列详解

### 一、什么是死信（Dead Letter）？

**死信**是指无法被正常消费的消息。当消息满足以下条件之一时，会变成死信：

#### 1. 消息过期（TTL过期）
```java
// 方式1：队列级别TTL（所有消息10秒过期）
Map<String, Object> args = new HashMap<>();
args.put("x-message-ttl", 10000);
Queue queue = new Queue("queue", true, false, false, args);

// 方式2：消息级别TTL（单独设置）
rabbitTemplate.convertAndSend(exchange, routingKey, message, m -> {
    m.getMessageProperties().setExpiration("5000");
    return m;
});
```

#### 2. 消息被拒绝（Basic.Reject / Basic.Nack）
```java
@RabbitListener(queues = "normal.queue")
public void receive(String message, Channel channel, 
                   @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
    // 拒绝消息且不重新入队 -> 变成死信
    channel.basicReject(tag, false);
    
    // 批量拒绝
    // channel.basicNack(tag, false, false);
}
```

#### 3. 队列达到最大长度
```java
Map<String, Object> args = new HashMap<>();
args.put("x-max-length", 10);  // 队列最多10条消息
Queue queue = new Queue("queue", true, false, false, args);
```

---

### 二、死信的处理机制

死信不会被自动丢弃，而是通过**死信交换机（Dead Letter Exchange, DLX）**路由到**死信队列（Dead Letter Queue, DLQ）**。

#### 配置死信队列：

```java
@Bean
public Queue normalQueue() {
    Map<String, Object> args = new HashMap<>();
    // 设置死信交换机
    args.put("x-dead-letter-exchange", "dead.letter.exchange");
    // 设置死信路由键（可选，默认使用原路由键）
    args.put("x-dead-letter-routing-key", "dead.letter.key");
    // 设置消息TTL
    args.put("x-message-ttl", 10000);
    
    return QueueBuilder.durable("normal.queue").withArguments(args).build();
}

// 死信交换机
@Bean
public DirectExchange deadLetterExchange() {
    return ExchangeBuilder.directExchange("dead.letter.exchange").build();
}

// 死信队列
@Bean
public Queue deadLetterQueue() {
    return QueueBuilder.durable("dead.letter.queue").build();
}

// 绑定死信队列到死信交换机
@Bean
public Binding deadLetterBinding() {
    return BindingBuilder.bind(deadLetterQueue())
            .to(deadLetterExchange())
            .with("dead.letter.key");
}
```

---

### 三、死信的解决方案

#### 方案1：告警 + 人工介入
```java
@RabbitListener(queues = "dead.letter.queue")
public void handleDeadLetter(String message) {
    // 1. 记录日志
    log.error("收到死信消息: {}", message);
    
    // 2. 发送告警（邮件、短信、钉钉等）
    alertService.sendAlert("死信队列有消息积压");
    
    // 3. 人工检查并处理
}
```

#### 方案2：自动重试机制
```java
@RabbitListener(queues = "dead.letter.queue")
public void handleDeadLetterWithRetry(String message, 
        @Header(name = "x-retry-count", defaultValue = "0") int retryCount) {
    if (retryCount < 3) {
        try {
            // 重试处理
            processMessage(message);
            
            // 成功后移除重试计数
            log.info("死信重试成功");
        } catch (Exception e) {
            // 增加重试计数并重新入队
            int newRetryCount = retryCount + 1;
            rabbitTemplate.convertAndSend("dead.letter.exchange", "key", message, m -> {
                m.getMessageProperties().setHeader("x-retry-count", newRetryCount);
                return m;
            });
        }
    } else {
        // 超过最大重试次数，记录到数据库
        log.error("死信重试次数耗尽，记录到数据库: {}", message);
        deadLetterRepository.save(message);
    }
}
```

#### 方案3：延迟队列实现
利用死信+TTL实现延迟队列：

```java
// 延迟队列配置
@Bean
public Queue delayQueue() {
    Map<String, Object> args = new HashMap<>();
    args.put("x-dead-letter-exchange", "order.delay.exchange");
    args.put("x-dead-letter-routing-key", "order.key");
    args.put("x-message-ttl", 60000);  // 延迟60秒
    return new Queue("order.delay.queue", true, false, false, args);
}

// 应用：订单超时自动取消
// 1. 创建订单后，发送消息到延迟队列
// 2. 60秒后消息过期进入死信队列
// 3. 死信消费者检查订单状态，如果未支付则取消
```

#### 方案4：补偿机制
```java
// 记录死信到数据库
@Entity
public class DeadLetterLog {
    private Long id;
    private String message;       // 消息内容
    private String queue;         // 来源队列
    private String exchange;      // 来源交换机
    private String routingKey;    // 路由键
    private String reason;        // 死信原因
    private String status;        // 状态：PENDING/PROCESSING/SUCCESS/FAILED
    private Integer retryCount;   // 重试次数
    private Date createTime;
}

// 定时任务扫描并重试
@Scheduled(fixedDelay = 60000)
public void retryDeadLetters() {
    List<DeadLetterLog> pendingList = deadLetterLogRepository
            .findByStatusAndRetryCountLessThan("PENDING", 3);
    
    for (DeadLetterLog log : pendingList) {
        try {
            // 重新发送到原队列
            rabbitTemplate.convertAndSend(
                log.getExchange(), 
                log.getRoutingKey(), 
                log.getMessage()
            );
            
            log.setStatus("SUCCESS");
        } catch (Exception e) {
            log.setRetryCount(log.getRetryCount() + 1);
        }
        deadLetterLogRepository.save(log);
    }
}
```

#### 方案5：版本号机制（防止重复处理）
```java
@Data
public class MessageWithVersion {
    private String id;
    private String content;
    private Integer version;  // 版本号
}

@RabbitListener(queues = "dead.letter.queue")
public void handleWithVersion(MessageWithVersion message) {
    // 检查版本号，避免重复处理
    String key = message.getId() + ":" + message.getVersion();
    if (redisTemplate.opsForValue().setIfAbsent(key, "1", 3600)) {
        // 第一次处理
        processMessage(message);
    } else {
        log.warn("消息已被处理，跳过: {}", message.getId());
    }
}
```

---

### 四、最佳实践

#### 1. 一定要配置死信队列
```yaml
# 生产环境必须配置死信队列
spring:
  rabbitmq:
    listener:
      simple:
        default-requeue-rejected: false  # 拒绝的消息不重新入队
```

#### 2. 监控死信队列
```java
// 监控死信队列长度
@Scheduled(fixedDelay = 60000)
public void monitorDeadLetterQueue() {
    int messageCount = rabbitTemplate.execute(channel -> 
        channel.queueDeclarePassive("dead.letter.queue").getMessageCount()
    );
    
    if (messageCount > 100) {
        alertService.sendAlert("死信队列积压: " + messageCount);
    }
}
```

#### 3. 区分业务异常和系统异常
```java
@RabbitListener(queues = "normal.queue")
public void receive(String message, Channel channel, long tag) {
    try {
        processMessage(message);
        channel.basicAck(tag, false);
    } catch (BusinessException e) {
        // 业务异常：消息格式错误，不应重试
        log.error("业务异常，直接进入死信队列", e);
        channel.basicReject(tag, false);
    } catch (SystemException e) {
        // 系统异常：网络抖动，可以重试
        log.error("系统异常，重新入队", e);
        channel.basicNack(tag, false, true);
    }
}
```

#### 4. 获取死信原因
RabbitMQ会在死信消息的headers中添加以下信息：
```java
@RabbitListener(queues = "dead.letter.queue")
public void handle(Message message) {
    Map<String, Object> headers = message.getMessageProperties().getHeaders();
    
    String reason = (String) headers.get("x-first-death-reason");
    // 可能的值：
    // - "expired"：消息过期
    // - "rejected"：消息被拒绝
    // - "maxlen"：队列已满
    
    String queue = (String) headers.get("x-first-death-queue");
    String exchange = (String) headers.get("x-first-death-exchange");
    
    log.error("死信原因: {}, 来源队列: {}, 来源交换机: {}", 
             reason, queue, exchange);
}
```

---

## 测试接口说明

启动项目后，访问 Swagger UI：http://localhost:8001/swagger-ui.html

### 接口列表

| 模式 | 接口 | 说明 |
|------|------|------|
| Hello World | POST /rabbitmq/hello | 简单模式 |
| Work Queues | POST /rabbitmq/work | 工作队列 |
| Work Queues | POST /rabbitmq/work/batch?count=10 | 批量发送 |
| Publish/Subscribe | POST /rabbitmq/fanout | 发布订阅 |
| Routing | POST /rabbitmq/direct/info | 路由-info |
| Routing | POST /rabbitmq/direct/error | 路由-error |
| Routing | POST /rabbitmq/direct/warning | 路由-warning |
| Topics | POST /rabbitmq/topic/user/add | 主题-user.add |
| Topics | POST /rabbitmq/topic/user/delete | 主题-user.delete |
| Topics | POST /rabbitmq/topic/order/create | 主题-order.create |
| RPC | POST /rabbitmq/rpc | RPC调用 |
| 死信-TTL | POST /rabbitmq/dead-letter/ttl?ttl=5000 | 消息过期 |
| 死信-拒绝 | POST /rabbitmq/dead-letter/reject | 消息被拒绝 |
| 死信-正常 | POST /rabbitmq/dead-letter/normal | 10秒后过期 |

### 查看文档
- 模式说明：GET /rabbitmq/docs
- 死信详解：GET /rabbitmq/dead-letter/docs

---

## Docker启动RabbitMQ

### 1. 拉取镜像
```bash
docker pull rabbitmq:3.12-management
```

### 2. 启动容器
```bash
docker run -d \
  --name rabbitmq \
  -p 5672:5672 \
  -p 15672:15672 \
  -e RABBITMQ_DEFAULT_USER=guest \
  -e RABBITMQ_DEFAULT_PASS=guest \
  rabbitmq:3.12-management
```

### 3. 访问管理界面
- URL: http://localhost:15672
- 用户名: guest
- 密码: guest

### 4. 常用命令
```bash
# 查看运行状态
docker ps | grep rabbitmq

# 查看日志
docker logs -f rabbitmq

# 进入容器
docker exec -it rabbitmq bash

# 停止容器
docker stop rabbitmq

# 启动容器
docker start rabbitmq
```

---

## 总结

本项目已实现：
1. ✅ RabbitMQ依赖集成
2. ✅ 六种工作模式完整示例
3. ✅ 死信队列配置和示例
4. ✅ 详细的测试接口
5. ✅ 死信问题解决方案文档

**下一步建议**：
1. 启动Docker中的RabbitMQ
2. 启动Spring Boot项目
3. 通过Swagger UI测试各种模式
4. 观察RabbitMQ管理界面的变化
5. 查看控制台日志理解消息流转

---

**作者**: AI Assistant  
**日期**: 2026-05-12
