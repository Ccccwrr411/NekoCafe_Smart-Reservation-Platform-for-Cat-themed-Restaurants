package cn.edu.bjfu.nekocafe.mq;

import cn.edu.bjfu.nekocafe.entity.OutboxMessage;
import cn.edu.bjfu.nekocafe.mapper.OutboxMessageMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 发件箱后台投递器：定时把 PENDING 消息可靠投递到 RabbitMQ。
 *
 * 可靠性要点：
 *   - 批次内用 SELECT ... FOR UPDATE SKIP LOCKED 锁住消息行（同事务），多实例并行不重复投递。
 *   - 用 publisher confirms（waitForConfirmsOrDie）确认 Broker 已接收，确认后才标记 SENT。
 *   - 发送/确认失败 → 不标记 SENT → 下个周期重试（至少一次；消费者侧幂等去重）。
 */
@Component
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);
    private static final int BATCH = 200;

    private final OutboxMessageMapper outboxMapper;
    private final RabbitTemplate rabbitTemplate;

    public OutboxPublisher(OutboxMessageMapper outboxMapper, RabbitTemplate rabbitTemplate) {
        this.outboxMapper = outboxMapper;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Scheduled(fixedDelayString = "${nekocafe.outbox.poll-interval-ms:500}")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void dispatch() {
        List<OutboxMessage> batch = outboxMapper.lockPendingBatch(BATCH);
        if (batch == null || batch.isEmpty()) {
            return;
        }
        for (OutboxMessage m : batch) {
            try {
                publishConfirmed(m);
                outboxMapper.markSent(m.getId());
            } catch (Exception e) {
                outboxMapper.incrementRetry(m.getId());
                log.warn("发件箱投递失败 id={} type={} 将在下个周期重试: {}", m.getId(), m.getMsgType(), e.toString());
            }
        }
    }

    private void publishConfirmed(OutboxMessage m) {
        MessageProperties props = new MessageProperties();
        props.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        props.setContentEncoding(StandardCharsets.UTF_8.name());
        props.setDeliveryMode(MessageDeliveryMode.PERSISTENT); // 消息持久化
        props.setMessageId(String.valueOf(m.getId()));
        Message message = new Message(m.getPayload().getBytes(StandardCharsets.UTF_8), props);

        Boolean ok = rabbitTemplate.invoke(ops -> {
            ops.send(m.getExchange(), m.getRoutingKey(), message);
            ops.waitForConfirmsOrDie(5000);
            return true;
        });
        if (ok == null || !ok) {
            throw new IllegalStateException("publisher confirm 未通过");
        }
    }
}
