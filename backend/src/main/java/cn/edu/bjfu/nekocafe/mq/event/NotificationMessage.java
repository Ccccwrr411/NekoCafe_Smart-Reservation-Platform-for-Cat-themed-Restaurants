package cn.edu.bjfu.nekocafe.mq.event;

import java.io.Serializable;

/**
 * 通知消息：把「门店端通知写库」从用户请求线程剥离，异步发送。
 * messageId 用于消费幂等（同一通知不重复落库）。
 */
public class NotificationMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String messageId;
    private Integer storeId;
    private Long userId;
    private String targetRole;
    private String type;
    private String title;
    private String content;
    private String relatedType;
    private Long relatedId;

    public NotificationMessage() {}

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public Integer getStoreId() { return storeId; }
    public void setStoreId(Integer storeId) { this.storeId = storeId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getTargetRole() { return targetRole; }
    public void setTargetRole(String targetRole) { this.targetRole = targetRole; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getRelatedType() { return relatedType; }
    public void setRelatedType(String relatedType) { this.relatedType = relatedType; }

    public Long getRelatedId() { return relatedId; }
    public void setRelatedId(Long relatedId) { this.relatedId = relatedId; }
}
