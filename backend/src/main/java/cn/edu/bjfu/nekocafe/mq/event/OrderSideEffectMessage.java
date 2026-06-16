package cn.edu.bjfu.nekocafe.mq.event;

import java.io.Serializable;
import java.util.List;

/**
 * 订单副作用消息：submitOrder 提交成功后，把「非支付强依赖」的事后处理异步化。
 * 消费者据此完成：会员累计金额/积分、积分流水、优惠券使用记录。
 *
 * messageId 取确定值 "ORDER_SE:" + reservationId，用于消费幂等与「取消/异步」结算协调。
 */
public class OrderSideEffectMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String messageId;
    private Long reservationId;
    private Long userId;
    private Integer finalAmount;     // 实付（分/元，沿用原 BigDecimal 语义）
    private Integer pointsEarned;    // 本单获得积分
    private List<String> couponIds;  // 使用的优惠券
    private Integer perCouponDiscount;

    public OrderSideEffectMessage() {}

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public Long getReservationId() { return reservationId; }
    public void setReservationId(Long reservationId) { this.reservationId = reservationId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Integer getFinalAmount() { return finalAmount; }
    public void setFinalAmount(Integer finalAmount) { this.finalAmount = finalAmount; }

    public Integer getPointsEarned() { return pointsEarned; }
    public void setPointsEarned(Integer pointsEarned) { this.pointsEarned = pointsEarned; }

    public List<String> getCouponIds() { return couponIds; }
    public void setCouponIds(List<String> couponIds) { this.couponIds = couponIds; }

    public Integer getPerCouponDiscount() { return perCouponDiscount; }
    public void setPerCouponDiscount(Integer perCouponDiscount) { this.perCouponDiscount = perCouponDiscount; }
}
