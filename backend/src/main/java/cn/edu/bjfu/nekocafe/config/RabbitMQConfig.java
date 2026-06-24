package cn.edu.bjfu.nekocafe.config;

import cn.edu.bjfu.nekocafe.mq.MqConst;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ 拓扑声明：Exchange / Queue / Binding / 死信 / 延迟队列。
 * 应用启动时由 RabbitAdmin 自动在 Broker 上创建（幂等）。
 */
@Configuration
public class RabbitMQConfig {

    // ============ 订单业务交换机（topic） ============
    @Bean
    public TopicExchange orderExchange() {
        return ExchangeBuilder.topicExchange(MqConst.ORDER_EXCHANGE).durable(true).build();
    }

    // ============ 死信交换机 ============
    @Bean
    public DirectExchange dlxExchange() {
        return ExchangeBuilder.directExchange(MqConst.DLX_EXCHANGE).durable(true).build();
    }

    // ---- 订单副作用队列（挂死信） ----
    @Bean
    public Queue orderSideEffectQueue() {
        return QueueBuilder.durable(MqConst.QUEUE_ORDER_SIDE_EFFECT)
                .withArgument("x-dead-letter-exchange", MqConst.DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", MqConst.RK_DLQ_SIDE_EFFECT)
                .build();
    }

    @Bean
    public Binding orderSideEffectBinding() {
        return BindingBuilder.bind(orderSideEffectQueue()).to(orderExchange()).with(MqConst.RK_ORDER_SIDE_EFFECT);
    }

    @Bean
    public Queue orderSideEffectDlq() {
        return QueueBuilder.durable(MqConst.QUEUE_DLQ_SIDE_EFFECT).build();
    }

    @Bean
    public Binding orderSideEffectDlqBinding() {
        return BindingBuilder.bind(orderSideEffectDlq()).to(dlxExchange()).with(MqConst.RK_DLQ_SIDE_EFFECT);
    }

    // ---- 通知队列（挂死信） ----
    @Bean
    public Queue orderNotificationQueue() {
        return QueueBuilder.durable(MqConst.QUEUE_ORDER_NOTIFICATION)
                .withArgument("x-dead-letter-exchange", MqConst.DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", MqConst.RK_DLQ_NOTIFICATION)
                .build();
    }

    @Bean
    public Binding orderNotificationBinding() {
        return BindingBuilder.bind(orderNotificationQueue()).to(orderExchange()).with(MqConst.RK_ORDER_NOTIFICATION);
    }

    @Bean
    public Queue orderNotificationDlq() {
        return QueueBuilder.durable(MqConst.QUEUE_DLQ_NOTIFICATION).build();
    }

    @Bean
    public Binding orderNotificationDlqBinding() {
        return BindingBuilder.bind(orderNotificationDlq()).to(dlxExchange()).with(MqConst.RK_DLQ_NOTIFICATION);
    }

    // ============ 预约超时自动取消：延迟队列（TTL + 死信转发） ============
    @Bean
    public DirectExchange reservationDelayExchange() {
        return ExchangeBuilder.directExchange(MqConst.RESERVATION_DELAY_EXCHANGE).durable(true).build();
    }

    @Bean
    public DirectExchange reservationTimeoutExchange() {
        return ExchangeBuilder.directExchange(MqConst.RESERVATION_TIMEOUT_EXCHANGE).durable(true).build();
    }

    /** 延迟队列：消息不被消费，TTL 到期后经死信转入超时交换机。 */
    @Bean
    public Queue reservationDelayQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", (int) MqConst.RESERVATION_TIMEOUT_MS);
        args.put("x-dead-letter-exchange", MqConst.RESERVATION_TIMEOUT_EXCHANGE);
        args.put("x-dead-letter-routing-key", MqConst.RK_RESERVATION_TIMEOUT);
        return QueueBuilder.durable(MqConst.RESERVATION_DELAY_QUEUE).withArguments(args).build();
    }

    @Bean
    public Binding reservationDelayBinding() {
        return BindingBuilder.bind(reservationDelayQueue())
                .to(reservationDelayExchange()).with(MqConst.RK_RESERVATION_DELAY);
    }

    /** 超时队列：实际被消费者监听，到期未支付的预约在此自动取消。 */
    @Bean
    public Queue reservationTimeoutQueue() {
        return QueueBuilder.durable(MqConst.RESERVATION_TIMEOUT_QUEUE).build();
    }

    @Bean
    public Binding reservationTimeoutBinding() {
        return BindingBuilder.bind(reservationTimeoutQueue())
                .to(reservationTimeoutExchange()).with(MqConst.RK_RESERVATION_TIMEOUT);
    }
}
