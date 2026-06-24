package cn.edu.bjfu.nekocafe.entity;

import java.util.Date;

/**
 * 本地消息表实体（对应 outbox_message）。
 */
public class OutboxMessage {
    private Long id;
    private String exchange;
    private String routingKey;
    private String payload;
    private String msgType;
    private String status;
    private Integer retryCount;
    private Date createdAt;
    private Date sentAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getExchange() { return exchange; }
    public void setExchange(String exchange) { this.exchange = exchange; }

    public String getRoutingKey() { return routingKey; }
    public void setRoutingKey(String routingKey) { this.routingKey = routingKey; }

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }

    public String getMsgType() { return msgType; }
    public void setMsgType(String msgType) { this.msgType = msgType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getSentAt() { return sentAt; }
    public void setSentAt(Date sentAt) { this.sentAt = sentAt; }
}
