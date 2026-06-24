package cn.edu.bjfu.nekocafe.mq;

/**
 * RabbitMQ 拓扑常量：Exchange / Queue / RoutingKey 名称集中管理。
 *
 * 拓扑总览：
 *   nekocafe.order.exchange (topic)
 *     ├─ rk=order.side-effect  → nekocafe.order.side-effect.queue  （积分/会员统计/优惠券使用记录）
 *     └─ rk=order.notification → nekocafe.order.notification.queue （门店新订单通知）
 *   两个业务队列都挂死信：消费重试耗尽 → nekocafe.dlx → 对应 .dlq
 *
 *   nekocafe.reservation.delay.exchange
 *     └─ nekocafe.reservation.delay.queue（带 TTL，消息到期经 DLX 转入超时队列）
 *   nekocafe.reservation.timeout.exchange
 *     └─ nekocafe.reservation.timeout.queue（到期未支付的预约自动取消）
 */
public final class MqConst {

    private MqConst() {}

    // ---- 订单业务 ----
    public static final String ORDER_EXCHANGE = "nekocafe.order.exchange";

    public static final String RK_ORDER_SIDE_EFFECT = "order.side-effect";
    public static final String RK_ORDER_NOTIFICATION = "order.notification";

    public static final String QUEUE_ORDER_SIDE_EFFECT = "nekocafe.order.side-effect.queue";
    public static final String QUEUE_ORDER_NOTIFICATION = "nekocafe.order.notification.queue";

    // ---- 死信 ----
    public static final String DLX_EXCHANGE = "nekocafe.dlx";
    public static final String RK_DLQ_SIDE_EFFECT = "dlq.order.side-effect";
    public static final String RK_DLQ_NOTIFICATION = "dlq.order.notification";
    public static final String QUEUE_DLQ_SIDE_EFFECT = "nekocafe.order.side-effect.dlq";
    public static final String QUEUE_DLQ_NOTIFICATION = "nekocafe.order.notification.dlq";

    // ---- 预约超时自动取消（延迟队列：TTL + 死信转发）----
    public static final String RESERVATION_DELAY_EXCHANGE = "nekocafe.reservation.delay.exchange";
    public static final String RESERVATION_DELAY_QUEUE = "nekocafe.reservation.delay.queue";
    public static final String RESERVATION_TIMEOUT_EXCHANGE = "nekocafe.reservation.timeout.exchange";
    public static final String RESERVATION_TIMEOUT_QUEUE = "nekocafe.reservation.timeout.queue";
    public static final String RK_RESERVATION_DELAY = "reservation.delay";
    public static final String RK_RESERVATION_TIMEOUT = "reservation.timeout";

    /** 预约创建后未支付的自动取消时限（毫秒）。延迟队列消息 TTL。 */
    public static final long RESERVATION_TIMEOUT_MS = 15 * 60 * 1000L;
}
