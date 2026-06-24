package cn.edu.bjfu.nekocafe.mq.consumer;

import cn.edu.bjfu.nekocafe.entity.Notifications;
import cn.edu.bjfu.nekocafe.mapper.MqConsumedMapper;
import cn.edu.bjfu.nekocafe.mapper.NotificationsMapper;
import cn.edu.bjfu.nekocafe.mq.MqConst;
import cn.edu.bjfu.nekocafe.mq.event.NotificationMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

/**
 * 通知消费者：异步把通知写入 notifications 表，避免阻塞下单/业务主线程。
 * 用 mq_consumed 做幂等，重复投递不会产生重复通知。
 */
@Component
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    private final MqConsumedMapper mqConsumedMapper;
    private final NotificationsMapper notificationsMapper;
    private final ObjectMapper objectMapper;

    public NotificationConsumer(MqConsumedMapper mqConsumedMapper,
                                NotificationsMapper notificationsMapper,
                                ObjectMapper objectMapper) {
        this.mqConsumedMapper = mqConsumedMapper;
        this.notificationsMapper = notificationsMapper;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = MqConst.QUEUE_ORDER_NOTIFICATION)
    @Transactional
    public void onMessage(Message message) {
        NotificationMessage evt;
        try {
            evt = objectMapper.readValue(new String(message.getBody(), StandardCharsets.UTF_8),
                    NotificationMessage.class);
        } catch (Exception e) {
            throw new AmqpRejectAndDontRequeueException("通知消息解析失败", e);
        }

        int claimed = mqConsumedMapper.tryClaim(evt.getMessageId(), true);
        if (claimed == 0) {
            log.info("通知已处理，跳过 messageId={}", evt.getMessageId());
            return;
        }

        Notifications n = new Notifications();
        n.setStoreId(evt.getStoreId());
        n.setUserId(evt.getUserId());
        n.setTargetRole(evt.getTargetRole());
        n.setType(evt.getType());
        n.setTitle(evt.getTitle());
        n.setContent(evt.getContent());
        n.setRelatedType(evt.getRelatedType());
        n.setRelatedId(evt.getRelatedId());
        n.setIsRead(false);
        notificationsMapper.insertSelective(n);

        log.info("通知已发送 storeId={} type={} relatedId={}", evt.getStoreId(), evt.getType(), evt.getRelatedId());
    }
}
