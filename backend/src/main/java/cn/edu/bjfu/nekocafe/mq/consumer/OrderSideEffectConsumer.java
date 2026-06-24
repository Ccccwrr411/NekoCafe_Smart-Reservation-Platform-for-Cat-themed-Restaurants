package cn.edu.bjfu.nekocafe.mq.consumer;

import cn.edu.bjfu.nekocafe.entity.CouponUsage;
import cn.edu.bjfu.nekocafe.entity.MemberExt;
import cn.edu.bjfu.nekocafe.entity.PointsLog;
import cn.edu.bjfu.nekocafe.mapper.CouponUsageMapper;
import cn.edu.bjfu.nekocafe.mapper.MemberExtMapper;
import cn.edu.bjfu.nekocafe.mapper.MqConsumedMapper;
import cn.edu.bjfu.nekocafe.mapper.PointsLogMapper;
import cn.edu.bjfu.nekocafe.mq.MqConst;
import cn.edu.bjfu.nekocafe.mq.event.OrderSideEffectMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * 订单副作用消费者：异步完成会员累计金额/积分、积分流水、优惠券使用记录。
 *
 * 幂等 + 结算协调：用 mq_consumed(message_id, applied=TRUE) 原子抢占。
 *   - 抢到（1）：副作用尚未结算，本事务内一并落库；applied=TRUE 与副作用同生共死。
 *   - 抢不到（0）：说明已被「取消订单」打墓碑(applied=FALSE)，或本消息已处理过 → 安全跳过。
 * 抛异常 → 事务回滚（含抢占记录）→ 消息重投 → 重试耗尽进入死信队列。
 */
@Component
public class OrderSideEffectConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderSideEffectConsumer.class);

    private final MqConsumedMapper mqConsumedMapper;
    private final MemberExtMapper memberExtMapper;
    private final PointsLogMapper pointsLogMapper;
    private final CouponUsageMapper couponUsageMapper;
    private final ObjectMapper objectMapper;

    public OrderSideEffectConsumer(MqConsumedMapper mqConsumedMapper,
                                   MemberExtMapper memberExtMapper,
                                   PointsLogMapper pointsLogMapper,
                                   CouponUsageMapper couponUsageMapper,
                                   ObjectMapper objectMapper) {
        this.mqConsumedMapper = mqConsumedMapper;
        this.memberExtMapper = memberExtMapper;
        this.pointsLogMapper = pointsLogMapper;
        this.couponUsageMapper = couponUsageMapper;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = MqConst.QUEUE_ORDER_SIDE_EFFECT)
    @Transactional
    public void onMessage(Message message) {
        OrderSideEffectMessage evt;
        try {
            evt = objectMapper.readValue(new String(message.getBody(), StandardCharsets.UTF_8),
                    OrderSideEffectMessage.class);
        } catch (Exception e) {
            // 反序列化失败属于「毒消息」，直接丢弃进死信，不重试
            throw new org.springframework.amqp.AmqpRejectAndDontRequeueException("订单副作用消息解析失败", e);
        }

        String messageId = evt.getMessageId();
        // 原子抢占：抢到才结算
        int claimed = mqConsumedMapper.tryClaim(messageId, true);
        if (claimed == 0) {
            log.info("订单副作用已结算或被取消打墓碑，跳过 messageId={}", messageId);
            return;
        }

        Long userId = evt.getUserId();
        Long reservationId = evt.getReservationId();
        int finalAmount = evt.getFinalAmount() != null ? evt.getFinalAmount() : 0;
        int pointsEarned = evt.getPointsEarned() != null ? evt.getPointsEarned() : 0;
        Date now = new Date();

        // 1. 会员累计金额 + 积分
        MemberExt memberExt = memberExtMapper.selectByPrimaryKey(userId);
        int balanceAfter;
        if (memberExt == null) {
            memberExt = new MemberExt();
            memberExt.setUserId(userId);
            memberExt.setLevel(1);
            memberExt.setCumulativeAmount(new BigDecimal(finalAmount));
            memberExt.setTotalPoints(finalAmount * 2);
            memberExt.setLastVisitTime(now);
            memberExt.setCreatedAt(now);
            memberExtMapper.insertSelective(memberExt);
            balanceAfter = memberExt.getTotalPoints();
        } else {
            BigDecimal oldCumulative = memberExt.getCumulativeAmount() != null
                    ? memberExt.getCumulativeAmount() : BigDecimal.ZERO;
            BigDecimal newCumulative = oldCumulative.add(new BigDecimal(finalAmount));
            int newTotalPoints = newCumulative.intValue() * 2;

            MemberExt updateMe = new MemberExt();
            updateMe.setUserId(userId);
            updateMe.setCumulativeAmount(newCumulative);
            updateMe.setTotalPoints(newTotalPoints);
            updateMe.setLastVisitTime(now);
            memberExtMapper.updateByPrimaryKeySelective(updateMe);
            balanceAfter = newTotalPoints;
        }

        // 2. 积分流水（ORDER_EARN）
        PointsLog earnLog = new PointsLog();
        earnLog.setUserId(userId);
        earnLog.setChangeAmount(pointsEarned);
        earnLog.setBalanceAfter(balanceAfter);
        earnLog.setSource("ORDER_EARN");
        earnLog.setReservationId(reservationId);
        earnLog.setCreatedAt(now);
        pointsLogMapper.insertSelective(earnLog);

        // 3. 优惠券使用记录
        if (evt.getCouponIds() != null && !evt.getCouponIds().isEmpty()) {
            int perCouponDiscount = evt.getPerCouponDiscount() != null ? evt.getPerCouponDiscount() : 0;
            for (String couponIdStr : evt.getCouponIds()) {
                CouponUsage usage = new CouponUsage();
                usage.setCouponId(Long.parseLong(couponIdStr));
                usage.setReservationId(reservationId);
                usage.setDiscountAmount(new BigDecimal(perCouponDiscount));
                usage.setUsedAt(now);
                couponUsageMapper.insertSelective(usage);
            }
        }

        log.info("订单副作用结算完成 reservationId={} userId={} points+={}", reservationId, userId, pointsEarned);
    }
}
