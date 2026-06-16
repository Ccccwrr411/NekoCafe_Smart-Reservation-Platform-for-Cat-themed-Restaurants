package cn.edu.bjfu.nekocafe.mq;

import cn.edu.bjfu.nekocafe.entity.OutboxMessage;
import cn.edu.bjfu.nekocafe.mapper.OutboxMessageMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

/**
 * 事务性发件箱写入入口。
 *
 * 在业务 @Transactional 方法内调用 enqueue(...)，消息以一条 DB 记录形式与业务数据
 * 同一事务落库；真正投递交给 {@link OutboxPublisher} 后台轮询。这样：
 *   - 业务回滚 → outbox 记录一起回滚 → 不会发出「幽灵消息」
 *   - 业务提交 → outbox 记录一定在 → 轮询器保证最终投递（至少一次）
 */
@Component
public class MessageOutbox {

    private final OutboxMessageMapper outboxMapper;
    private final ObjectMapper objectMapper;

    public MessageOutbox(OutboxMessageMapper outboxMapper, ObjectMapper objectMapper) {
        this.outboxMapper = outboxMapper;
        this.objectMapper = objectMapper;
    }

    /**
     * 把一个事件加入发件箱（必须在调用方的事务中执行）。
     */
    public void enqueue(String exchange, String routingKey, String msgType, Object event) {
        String payload;
        try {
            payload = objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            throw new IllegalStateException("发件箱消息序列化失败: " + msgType, e);
        }
        OutboxMessage row = new OutboxMessage();
        row.setExchange(exchange);
        row.setRoutingKey(routingKey);
        row.setPayload(payload);
        row.setMsgType(msgType);
        outboxMapper.insert(row);
    }
}
