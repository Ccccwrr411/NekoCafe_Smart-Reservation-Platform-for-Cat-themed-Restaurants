package cn.edu.bjfu.nekocafe.dto;

/**
 * DTO - 改约请求体（对应接口 E-5）
 */
public class RescheduleDTO {
    private String orderId;
    private String newReserveDate;
    private String newReserveTime;

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getNewReserveDate() { return newReserveDate; }
    public void setNewReserveDate(String newReserveDate) { this.newReserveDate = newReserveDate; }
    public String getNewReserveTime() { return newReserveTime; }
    public void setNewReserveTime(String newReserveTime) { this.newReserveTime = newReserveTime; }
}
