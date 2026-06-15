package cn.edu.bjfu.nekocafe.dto;

/**
 * 店员提交考勤异常申请（请假/加班/调班）
 */
public class ShiftExceptionDTO {
    private Integer storeId;
    private Long staffId;
    private String exceptionDate;  // yyyy-MM-dd
    private String type;           // LEAVE / OVERTIME / SWAP
    private String reason;

    public Integer getStoreId() { return storeId; }
    public void setStoreId(Integer storeId) { this.storeId = storeId; }

    public Long getStaffId() { return staffId; }
    public void setStaffId(Long staffId) { this.staffId = staffId; }

    public String getExceptionDate() { return exceptionDate; }
    public void setExceptionDate(String exceptionDate) { this.exceptionDate = exceptionDate; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
