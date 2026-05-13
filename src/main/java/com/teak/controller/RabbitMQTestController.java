package com.teak.controller;

import com.teak.rabbitmq.producer.RabbitMQProducer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * RabbitMQ 测试 Controller
 * 提供各种RabbitMQ模式的测试接口
 */
@Slf4j
@RestController
@RequestMapping("/rabbitmq")
@RequiredArgsConstructor
@Tag(name = "RabbitMQ测试接口", description = "测试RabbitMQ的各种工作模式")
public class RabbitMQTestController {

    private final RabbitMQProducer rabbitMQProducer;

    // ==================== 1. Hello World 简单模式 ====================
    
    @Operation(summary = "测试Hello World模式")
    @PostMapping("/hello")
    public String testHello(@Parameter(description = "消息内容") @RequestParam(defaultValue = "Hello World") String message) {
        rabbitMQProducer.sendHelloMessage(message);
        return "Hello World模式消息已发送: " + message;
    }

    // ==================== 2. Work Queues 工作队列模式 ====================
    
    @Operation(summary = "测试Work Queues模式")
    @PostMapping("/work")
    public String testWork(@Parameter(description = "消息内容") @RequestParam(defaultValue = "Work Message") String message) {
        rabbitMQProducer.sendWorkMessage(message);
        return "Work Queues模式消息已发送: " + message;
    }

    @Operation(summary = "批量测试Work Queues模式")
    @PostMapping("/work/batch")
    public String testWorkBatch(@Parameter(description = "消息数量") @RequestParam(defaultValue = "10") int count) {
        for (int i = 1; i <= count; i++) {
            rabbitMQProducer.sendWorkMessage("Work Message " + i);
        }
        return "Work Queues模式已发送 " + count + " 条消息";
    }

    // ==================== 3. Publish/Subscribe 发布订阅模式 ====================
    
    @Operation(summary = "测试Publish/Subscribe模式")
    @PostMapping("/fanout")
    public String testFanout(@Parameter(description = "消息内容") @RequestParam(defaultValue = "Fanout Message") String message) {
        rabbitMQProducer.sendFanoutMessage(message);
        return "Publish/Subscribe模式消息已发送（会广播到所有队列）: " + message;
    }

    // ==================== 4. Routing 路由模式 ====================
    
    @Operation(summary = "测试Routing模式 - Info日志")
    @PostMapping("/direct/info")
    public String testDirectInfo(@Parameter(description = "消息内容") @RequestParam(defaultValue = "Info Log") String message) {
        rabbitMQProducer.sendDirectInfoMessage(message);
        return "Routing模式INFO消息已发送: " + message;
    }

    @Operation(summary = "测试Routing模式 - Error日志")
    @PostMapping("/direct/error")
    public String testDirectError(@Parameter(description = "消息内容") @RequestParam(defaultValue = "Error Log") String message) {
        rabbitMQProducer.sendDirectErrorMessage(message);
        return "Routing模式ERROR消息已发送: " + message;
    }

    @Operation(summary = "测试Routing模式 - Warning日志")
    @PostMapping("/direct/warning")
    public String testDirectWarning(@Parameter(description = "消息内容") @RequestParam(defaultValue = "Warning Log") String message) {
        rabbitMQProducer.sendDirectWarningMessage(message);
        return "Routing模式WARNING消息已发送（会路由到Error队列）: " + message;
    }

    // ==================== 5. Topics 主题模式 ====================
    
    @Operation(summary = "测试Topics模式 - user.add")
    @PostMapping("/topic/user/add")
    public String testTopicUserAdd(@Parameter(description = "消息内容") @RequestParam(defaultValue = "Add User") String message) {
        rabbitMQProducer.sendTopicUserAddMessage(message);
        return "Topics模式 user.add 消息已发送: " + message;
    }

    @Operation(summary = "测试Topics模式 - user.delete")
    @PostMapping("/topic/user/delete")
    public String testTopicUserDelete(@Parameter(description = "消息内容") @RequestParam(defaultValue = "Delete User") String message) {
        rabbitMQProducer.sendTopicUserDeleteMessage(message);
        return "Topics模式 user.delete 消息已发送: " + message;
    }

    @Operation(summary = "测试Topics模式 - order.create")
    @PostMapping("/topic/order/create")
    public String testTopicOrderCreate(@Parameter(description = "消息内容") @RequestParam(defaultValue = "Create Order") String message) {
        rabbitMQProducer.sendTopicOrderCreateMessage(message);
        return "Topics模式 order.create 消息已发送（只会到队列2）: " + message;
    }

    // ==================== 6. RPC 模式 ====================
    
    @Operation(summary = "测试RPC模式")
    @PostMapping("/rpc")
    public String testRPC(@Parameter(description = "消息内容") @RequestParam(defaultValue = "RPC Request") String message) {
        String response = rabbitMQProducer.sendRPCMessage(message);
        return "RPC模式响应: " + response;
    }

    // ==================== 7. 死信队列模式 ====================
    
    @Operation(summary = "测试死信队列 - 消息过期")
    @PostMapping("/dead-letter/ttl")
    public String testDeadLetterTTL(@Parameter(description = "消息内容") @RequestParam(defaultValue = "TTL Message") String message,
                                    @Parameter(description = "过期时间(毫秒)") @RequestParam(defaultValue = "5000") int ttl) {
        rabbitMQProducer.sendMessageWithTTL(message, ttl);
        return "死信队列模式消息已发送（TTL=" + ttl + "ms）: " + message + "，等待过期后会进入死信队列";
    }

    @Operation(summary = "测试死信队列 - 消息被拒绝")
    @PostMapping("/dead-letter/reject")
    public String testDeadLetterReject(@Parameter(description = "消息内容") @RequestParam(defaultValue = "Reject Message") String message) {
        rabbitMQProducer.sendRejectMessage(message);
        return "死信队列模式消息已发送（会被拒绝进入死信队列）: " + message;
    }

    @Operation(summary = "测试死信队列 - 普通消息")
    @PostMapping("/dead-letter/normal")
    public String testDeadLetterNormal(@Parameter(description = "消息内容") @RequestParam(defaultValue = "Normal Message") String message) {
        rabbitMQProducer.sendNormalMessage(message);
        return "死信队列模式普通消息已发送（10秒后过期进入死信队列）: " + message;
    }

    // ==================== 文档说明接口 ====================
    
    @Operation(summary = "获取RabbitMQ模式说明")
    @GetMapping("/docs")
    public String getDocs() {
        return """
                RabbitMQ 六种工作模式：
                
                1. Hello World（简单模式）
                   - POST /rabbitmq/hello
                   - 一个生产者一个消费者
                   
                2. Work Queues（工作队列模式）
                   - POST /rabbitmq/work
                   - POST /rabbitmq/work/batch?count=10
                   - 一个队列多个消费者，竞争消费
                   
                3. Publish/Subscribe（发布订阅模式）
                   - POST /rabbitmq/fanout
                   - 使用Fanout交换机，广播到所有绑定队列
                   
                4. Routing（路由模式）
                   - POST /rabbitmq/direct/info
                   - POST /rabbitmq/direct/error
                   - POST /rabbitmq/direct/warning
                   - 使用Direct交换机，根据路由键精确匹配
                   
                5. Topics（主题模式）
                   - POST /rabbitmq/topic/user/add
                   - POST /rabbitmq/topic/user/delete
                   - POST /rabbitmq/topic/order/create
                   - 使用Topic交换机，根据路由键模式匹配（*和#）
                   
                6. RPC（远程调用模式）
                   - POST /rabbitmq/rpc
                   - 客户端发送消息并等待响应
                   
                7. Dead Letter Queue（死信队列模式）
                   - POST /rabbitmq/dead-letter/ttl?ttl=5000
                   - POST /rabbitmq/dead-letter/reject
                   - POST /rabbitmq/dead-letter/normal
                   - 处理无法被正常消费的消息
                   
                死信队列文档：
                   GET /rabbitmq/dead-letter/docs
                """;
    }

    @Operation(summary = "获取死信队列详细说明")
    @GetMapping("/dead-letter/docs")
    public String getDeadLetterDocs() {
        return """
                ========================================
                RabbitMQ 死信（Dead Letter）详解
                ========================================
                
                一、什么是死信？
                
                死信（Dead Letter）是指无法被正常消费的消息。
                当消息满足以下条件之一时，会变成死信：
                
                1. 消息过期（TTL过期）
                   - 队列设置TTL：x-message-ttl
                   - 消息单独设置TTL：expiration属性
                   
                2. 消息被拒绝（Basic.Reject / Basic.Nack）
                   - 并且requeue=false（不重新入队）
                   
                3. 队列达到最大长度
                   - 设置了x-max-length或x-max-length-bytes
                   - 新消息会挤掉旧消息，被挤掉的消息变成死信
                
                ========================================
                
                二、死信的处理机制
                
                死信不会被自动丢弃，而是通过死信交换机（Dead Letter Exchange, DLX）
                路由到死信队列（Dead Letter Queue, DLQ）进行处理。
                
                配置死信队列：
                1. 在普通队列中设置死信交换机：x-dead-letter-exchange
                2. 在普通队列中设置死信路由键：x-dead-letter-routing-key
                
                ========================================
                
                三、死信的解决方案
                
                方案1：告警 + 人工介入
                   - 监控死信队列长度
                   - 发送告警通知
                   - 人工检查死信消息并处理
                   
                方案2：自动重试机制
                   - 死信队列的消费者尝试重新处理
                   - 设置最大重试次数
                   - 超过次数后记录到数据库或文件
                   
                方案3：延迟队列实现
                   - 使用死信+TTL实现延迟队列
                   - 消息过期后进入死信队列进行延迟处理
                   
                方案4：补偿机制
                   - 记录死信消息到数据库
                   - 定时任务扫描并重试
                   - 提供手动重试接口
                   
                方案5：版本号机制
                   - 消息携带版本号
                   - 消费时检查版本，避免重复处理
                
                ========================================
                
                四、最佳实践
                
                1. 一定要配置死信队列
                   - 避免消息丢失
                   - 方便问题排查
                   
                2. 监控死信队列
                   - 设置告警阈值
                   - 定期清理或处理
                   
                3. 记录死信原因
                   - RabbitMQ会添加headers：
                     x-first-death-reason: 死信原因
                     x-first-death-queue: 来源队列
                     x-first-death-exchange: 来源交换机
                   
                4. 区分业务异常和系统异常
                   - 业务异常：消息格式错误，不应重试
                   - 系统异常：网络抖动，可以重试
                   
                5. 设置合理的TTL和重试次数
                   - 避免无限重试
                   - 避免消息积压
                
                ========================================
                
                五、本项目中的死信队列测试
                
                1. 测试消息过期：
                   POST /rabbitmq/dead-letter/ttl?ttl=5000
                   - 5秒后消息过期，进入死信队列
                   
                2. 测试消息被拒绝：
                   POST /rabbitmq/dead-letter/reject
                   - 消息以"REJECT:"开头，会被拒绝进入死信队列
                   
                3. 测试队列满（需要修改配置）：
                   - 设置x-max-length=10
                   - 发送超过10条消息
                   
                查看死信队列：
                   - 队列名：dead.letter.queue
                   - 交换机：dead.letter.exchange
                """;
    }

    // ==================== 诊断接口 ====================
    
    @Operation(summary = "诊断RabbitMQ连接")
    @GetMapping("/diagnose")
    public String diagnose() {
        StringBuilder result = new StringBuilder();
        result.append("=== RabbitMQ 诊断 ===\n\n");
        
        // 1. 检查RabbitTemplate
        try {
            result.append("1. RabbitTemplate: ").append(rabbitMQProducer != null ? "✅ 已注入" : "❌ 未注入").append("\n");
        } catch (Exception e) {
            result.append("1. RabbitTemplate: ❌ 错误 - ").append(e.getMessage()).append("\n");
        }
        
        // 2. 测试发送消息
        try {
            rabbitMQProducer.sendHelloMessage("诊断测试消息");
            result.append("2. 发送消息: ✅ 成功\n");
        } catch (Exception e) {
            result.append("2. 发送消息: ❌ 失败 - ").append(e.getMessage()).append("\n");
        }
        
        // 3. 检查队列（通过RabbitMQ API）
        result.append("3. 查看队列: 请访问 http://localhost:15672\n");
        result.append("   用户名/密码: guest/guest\n");
        result.append("   应该看到以下队列：\n");
        result.append("   - hello.queue\n");
        result.append("   - work.queue\n");
        result.append("   - fanout.queue.1\n");
        result.append("   - fanout.queue.2\n");
        result.append("   - direct.queue.info\n");
        result.append("   - direct.queue.error\n");
        result.append("   - topic.queue.1\n");
        result.append("   - topic.queue.2\n");
        result.append("   - normal.queue\n");
        result.append("   - dead.letter.queue\n");
        
        // 4. 检查消费者
        result.append("\n4. 消费者状态: 请查看应用日志\n");
        result.append("   如果看到 '接收到消息' 日志，说明消费者正常工作\n");
        result.append("   如果没有，检查：\n");
        result.append("   - RabbitMQ连接是否正常\n");
        result.append("   - 队列是否被创建\n");
        result.append("   - 消费者代码是否有错误\n");
        
        return result.toString();
    }
}
