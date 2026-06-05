package cn.edu.bjfu.nekocafe.dto;

/**
 * DTO - 创建预约请求体（对应接口 E-7）
 */
public class ReservationCreateDTO {
    private Integer storeId;
    private Integer tableId;
    private String reserveDate;   // yyyy-MM-dd
    private String reserveTime;   // HH:mm
    private Integer persons;
    private Integer duration;     // 小时

    public Integer getStoreId() { return storeId; }
    public void setStoreId(Integer storeId) { this.storeId = storeId; }
    public Integer getTableId() { return tableId; }
    public void setTableId(Integer tableId) { this.tableId = tableId; }
    public String getReserveDate() { return reserveDate; }
    public void setReserveDate(String reserveDate) { this.reserveDate = reserveDate; }
    public String getReserveTime() { return reserveTime; }
    public void setReserveTime(String reserveTime) { this.reserveTime = reserveTime; }
    public Integer getPersons() { return persons; }
    public void setPersons(Integer persons) { this.persons = persons; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
}
