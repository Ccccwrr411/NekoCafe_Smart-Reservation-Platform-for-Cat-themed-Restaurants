package cn.edu.bjfu.nekocafe.mq.consumer;

import cn.edu.bjfu.nekocafe.entity.Reservations;
import cn.edu.bjfu.nekocafe.entity.TableStatus;
import cn.edu.bjfu.nekocafe.mapper.ReservationsMapper;
import cn.edu.bjfu.nekocafe.mapper.TableStatusMapper;
import cn.edu.bjfu.nekocafe.mq.MqConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 预约超时自动取消消费者：延迟队列消息到期后到达本队列。
 * 若预约仍是 BOOKED（创建后一直未点单支付）→ 自动取消并释放桌位，避免桌位长期被占用。
 *
 * 幂等：仅当状态仍为 BOOKED 时才动作，重复投递无副作用。
 */
@Component
public class ReservationTimeoutConsumer {

    private static final Logger log = LoggerFactory.getLogger(ReservationTimeoutConsumer.class);
    private static final List<String> ACTIVE_STATUSES = Arrays.asList("BOOKED", "CONFIRMED");

    private final ReservationsMapper reservationsMapper;
    private final TableStatusMapper tableStatusMapper;

    public ReservationTimeoutConsumer(ReservationsMapper reservationsMapper,
                                      TableStatusMapper tableStatusMapper) {
        this.reservationsMapper = reservationsMapper;
        this.tableStatusMapper = tableStatusMapper;
    }

    @RabbitListener(queues = MqConst.RESERVATION_TIMEOUT_QUEUE)
    @Transactional
    public void onTimeout(Message message) {
        Long reservationId;
        try {
            reservationId = Long.parseLong(new String(message.getBody(), StandardCharsets.UTF_8).trim());
        } catch (Exception e) {
            log.warn("预约超时消息体非法，丢弃: {}", new String(message.getBody(), StandardCharsets.UTF_8));
            return;
        }

        Reservations reservation = reservationsMapper.selectByPrimaryKey(reservationId);
        if (reservation == null || !"BOOKED".equals(reservation.getStatus())) {
            return; // 已支付/已取消/已改约 → 幂等跳过
        }

        Date now = new Date();
        Reservations update = new Reservations();
        update.setReservationId(reservationId);
        update.setStatus("CANCEL_BOOKING");
        update.setUpdatedAt(now);
        reservationsMapper.updateByPrimaryKeySelective(update);

        // 该桌无其他活跃预约时释放桌位
        if (reservation.getTableId() != null) {
            int otherCount = reservationsMapper.countActiveByTableIdExcluding(
                    reservation.getTableId(), ACTIVE_STATUSES, reservationId);
            if (otherCount == 0) {
                TableStatus ts = tableStatusMapper.selectByPrimaryKey(reservation.getTableId());
                if (ts != null) {
                    tableStatusMapper.releaseTableOptimistic(
                            reservation.getTableId(), reservationId, ts.getVersion());
                }
            }
        }
        log.info("预约超时未支付，已自动取消 reservationId={}", reservationId);
    }
}
