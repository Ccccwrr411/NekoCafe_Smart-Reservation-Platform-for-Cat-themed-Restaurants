package cn.edu.bjfu.nekocafe.entity;

import java.util.Date;

public class Queue {
    private Long queueId;

    private Integer storeId;

    private Long userId;

    private Integer partySize;

    private String preferredTableType;

    private String status;

    private String queueNumber;

    private Date calledAt;

    private Integer seatedTableId;

    private Date createdAt;

    public Long getQueueId() {
        return queueId;
    }

    public void setQueueId(Long queueId) {
        this.queueId = queueId;
    }

    public Integer getStoreId() {
        return storeId;
    }

    public void setStoreId(Integer storeId) {
        this.storeId = storeId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getPartySize() {
        return partySize;
    }

    public void setPartySize(Integer partySize) {
        this.partySize = partySize;
    }

    public String getPreferredTableType() {
        return preferredTableType;
    }

    public void setPreferredTableType(String preferredTableType) {
        this.preferredTableType = preferredTableType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getQueueNumber() {
        return queueNumber;
    }

    public void setQueueNumber(String queueNumber) {
        this.queueNumber = queueNumber;
    }

    public Date getCalledAt() {
        return calledAt;
    }

    public void setCalledAt(Date calledAt) {
        this.calledAt = calledAt;
    }

    public Integer getSeatedTableId() {
        return seatedTableId;
    }

    public void setSeatedTableId(Integer seatedTableId) {
        this.seatedTableId = seatedTableId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}