# RabbitMQ 测试指南

## 一、启动准备

### 1. 确认RabbitMQ已启动
```bash
# 检查Docker容器状态
docker ps | findstr rabbitmq

# 如果未启动，执行
docker start rabbitmq

# 查看RabbitMQ日志
docker logs -f rabbitmq
```

### 2. 访问RabbitMQ管理界面
- URL: http://localhost:15672
- 用户名: guest
- 密码: guest

如果无法访问，检查：
```bash
# 检查端口映射
docker port rabbitmq

# 应该看到：
# 15672/tcp -> 0.0.0.0:15672
# 5672/tcp -> 0.0.0.0:5672
```

---

## 二、编译并启动项目

### 1. 编译项目（首次需要下载RabbitMQ依赖）
```bash
cd D:\idealProjects\teakWeb2
.\mvnw.cmd clean package -DskipTests
```

等待编译完成，应该看到 `BUILD SUCCESS`。

### 2. 启动Spring Boot应用
```bash
.\mvnw.cmd spring-boot:run
```

或者直接在IDE中运行 `TeakWeb2Application.java`

### 3. 观察启动日志
启动时应看到类似日志：
```
INFO  o.s.a.r.c.CachingConnectionFactory - Attempting to connect to: localhost:5672
INFO  o.s.a.r.c.CachingConnectionFactory - Created new connection
INFO  o.s.a.r.l.SimpleMessageListenerContainer - Waiting for messages...
```

如果看到连接失败，检查：
- RabbitMQ是否启动
- 配置是否正确（application-dev.yaml中的rabbitmq配置）

---

## 三、测试RabbitMQ六种模式

### 方式1：使用Swagger UI（推荐）

访问: http://localhost:8001/swagger-ui.html

找到 `RabbitMQ测试接口` 分组，依次测试：

#### 1. Hello World 模式
- 点击 `POST /rabbitmq/hello`
- 点击 `Try it out`
- 输入消息内容（或留空使用默认值）
- 点击 `Execute`
- 查看响应
- **检查控制台日志**，应该看到：
  ```
  INFO  c.t.r.producer.RabbitMQProducer - 【Hello World模式】发送消息: Hello World
  INFO  c.t.r.consumer.RabbitMQConsumer - 【Hello World模式】接收到消息: Hello World
  ```

#### 2. Work Queues 模式
- `POST /rabbitmq/work` - 发送单条消息
- `POST /rabbitmq/work/batch?count=10` - 批量发送10条
- 观察两个消费者如何竞争消费

#### 3. Publish/Subscribe 模式
- `POST /rabbitmq/fanout`
- 观察两个消费者都收到相同消息（广播）

#### 4. Routing 模式
- `POST /rabbitmq/direct/info` - 只有info队列收到
- `POST /rabbitmq/direct/error` - 只有error队列收到
- `POST /rabbitmq/direct/warning` - error队列也会收到（因为绑定了warning路由键）

#### 5. Topics 模式
- `POST /rabbitmq/topic/user/add` - 匹配 `user.*`，队列1和队列2都收到
- `POST /rabbitmq/topic/user/delete` - 匹配 `user.*`，队列1和队列2都收到
- `POST /rabbitmq/topic/order/create` - 只匹配 `#`，只有队列2收到

#### 6. RPC 模式
- `POST /rabbitmq/rpc`
- 会同步等待响应，返回: `RPC Response: RPC REQUEST`

#### 7. 死信队列模式
- `POST /rabbitmq/dead-letter/ttl?ttl=5000` - 5秒后过期进入死信队列
- `POST /rabbitmq/dead-letter/reject` - 立即被拒绝进入死信队列
- `POST /rabbitmq/dead-letter/normal` - 10秒后过期

---

### 方式2：使用curl命令

```bash
# Hello World
curl -X POST "http://localhost:8001/rabbitmq/hello?message=test"

# Work Queues
curl -X POST "http://localhost:8001/rabbitmq/work?message=test"

# Fanout
curl -X POST "http://localhost:8001/rabbitmq/fanout?message=test"

# Direct
curl -X POST "http://localhost:8001/rabbitmq/direct/info?message=test"

# Topics
curl -X POST "http://localhost:8001/rabbitmq/topic/user/add?message=test"

# RPC
curl -X POST "http://localhost:8001/rabbitmq/rpc?message=test"

# 死信队列
curl -X POST "http://localhost:8001/rabbitmq/dead-letter/ttl?ttl=5000"
```

---

## 四、在RabbitMQ管理界面验证

访问 http://localhost:15672

### 1. 查看队列（Queues标签页）
应该看到以下队列：
- `hello.queue`
- `work.queue`
- `fanout.queue.1`
- `fanout.queue.2`
- `direct.queue.info`
- `direct.queue.error`
- `topic.queue.1`
- `topic.queue.2`
- `rpc.queue`
- `normal.queue`
- `dead.letter.queue`

### 2. 查看交换机（Exchanges标签页）
应该看到以下交换机：
- `fanout.exchange`
- `direct.exchange`
- `topic.exchange`
- `rpc.exchange`
- `normal.exchange`
- `dead.letter.exchange`

### 3. 查看绑定关系（点击交换机，然后点击Bindings）
验证绑定是否正确。

### 4. 监控消息流转
1. 在管理界面点击队列名称
2. 点击 `Get Messages` 可以查看队列中的消息
3. 查看 `Overview` 标签页的图表，观察消息速率

---

## 五、查看日志

### 应用日志位置
`D:\idealProjects\teakWeb2\logs\`

### 关键日志
```bash
# 实时查看日志
Get-Content D:\idealProjects\teakWeb2\logs\spring.log -Wait

# 或者查看控制台输出
```

### 成功标志
- 生产者日志：`【XXX模式】发送消息: ...`
- 消费者日志：`【XXX模式】接收到消息: ...`

---

## 六、常见问题排查

### 问题1：消息发送成功但没有被消费
**原因**：消费者没有正确确认消息
**解决**：确保消费者方法添加了`Channel`和`@Header(AmqpHeaders.DELIVERY_TAG)`参数，并调用`channel.basicAck()`

### 问题2：RabbitMQ连接失败
**错误信息**：`Connection refused` 或 `Unable to connect`
**解决**：
```bash
# 检查RabbitMQ是否启动
docker ps | findstr rabbitmq

# 检查端口是否可访问
telnet localhost 5672
```

### 问题3：队列/交换机没有创建
**原因**：应用可能没有执行配置类
**解决**：
- 检查`RabbitMQConfig.java`是否有`@Configuration`注解
- 重启应用
- 查看启动日志是否有错误

### 问题4：手动确认模式报错
**错误信息**：`Channel closed`
**解决**：确保每次消费都调用了`basicAck`或`basicNack`

---

## 七、测试死信队列

### 测试步骤
1. 发送消息到普通队列：
   ```bash
   curl -X POST "http://localhost:8001/rabbitmq/dead-letter/ttl?ttl=5000"
   ```

2. 立即查看RabbitMQ管理界面：
   - 进入 `normal.queue`
   - 应该看到有1条消息

3. 等待5秒（TTL过期）

4. 刷新管理界面：
   - `normal.queue` 的消息数变为0
   - `dead.letter.queue` 的消息数变为1

5. 查看应用日志：
   ```
   ERROR c.t.r.consumer.RabbitMQConsumer - 【死信队列模式 - 死信队列】接收到死信消息: ...
   ERROR c.t.r.consumer.RabbitMQConsumer - 【死信队列模式】死信原因: expired
   ```

---

## 八、性能测试（可选）

### 批量发送消息
```bash
# 发送100条消息到Work Queue
for ($i=1; $i -le 100; $i++) {
    curl -X POST "http://localhost:8001/rabbitmq/work?message=Message$i"
}
```

### 观察消费者处理速度
- 消费者1处理一条消息需要1秒
- 消费者2处理一条消息需要2秒
- 总共100条消息，两个消费者竞争消费

---

## 九、清理测试数据

### 删除队列中的所有消息
在RabbitMQ管理界面：
1. 点击队列名称
2. 展开 `Delete / Purge`
3. 点击 `Purge Messages`

### 删除队列和交换机
```bash
# 使用RabbitMQ管理API
curl -u guest:guest -X DELETE "http://localhost:15672/api/queues/%2F/hello.queue"
```

---

## 十、下一步学习建议

1. **消息持久化**：确保消息不会因RabbitMQ重启而丢失
2. **消息确认机制**：生产者确认、消费者确认
3. **优先级队列**：实现消息优先级
4. **延迟队列**：实现定时任务
5. **消息追踪**：使用Firehose或rabbitmq_tracing插件
6. **集群和高可用**：生产环境部署

---

**祝测试顺利！**
