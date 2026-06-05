package cn.edu.bjfu.nekocafe.entity;

import java.util.Date;

public class ShiftExceptions {
    private Long exceptionId;

    private Integer storeId;

    private Long staffId;

    private Date exceptionDate;

    private String type;

    private Long originalScheduleId;

    private Long newScheduleId;

    private String status;

    private Long approverId;

    private String reason;

    private Date createdAt;

    public Long getExceptionId() {
        return exceptionId;
    }

    public void setExceptionId(Long exceptionId) {
        this.exceptionId = exceptionId;
    }

    public Integer getStoreId() {
        return storeId;
    }

    public void setStoreId(Integer storeId) {
        this.storeId = storeId;
    }

    public Long getStaffId() {
        return staffId;
    }

    public void setStaffId(Long staffId) {
        this.staffId = staffId;
    }

    public Date getExceptionDate() {
        return exceptionDate;
    }

    public void setExceptionDate(Date exceptionDate) {
        this.exceptionDate = exceptionDate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getOriginalScheduleId() {
        return originalScheduleId;
    }

    public void setOriginalScheduleId(Long originalScheduleId) {
        this.originalScheduleId = originalScheduleId;
    }

    public Long getNewScheduleId() {
        return newScheduleId;
    }

    public void setNewScheduleId(Long newScheduleId) {
        this.newScheduleId = newScheduleId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getApproverId() {
        return approverId;
    }

    public void setApproverId(Long approverId) {
        this.approverId = approverId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}