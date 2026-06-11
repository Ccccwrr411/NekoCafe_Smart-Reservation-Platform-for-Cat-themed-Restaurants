package cn.edu.bjfu.nekocafe.vo;

import java.math.BigDecimal;

/**
 * VO - 支付信息返回
 * 对应接口：POST /api/pay/create、GET /api/pay/status
 */
public class PayVO {
    private Long paymentId;
    private String orderId;
    private BigDecimal amount;
    private String status;       // pending / paid / failed / refunded
    private String transactionId;
    private String paidAt;

    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public String getPaidAt() { return paidAt; }
    public void setPaidAt(String paidAt) { this.paidAt = paidAt; }
}
