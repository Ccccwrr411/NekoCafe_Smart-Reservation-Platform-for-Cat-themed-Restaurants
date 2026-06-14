package cn.edu.bjfu.nekocafe.vo;

/**
 * VO - 当前预约信息（供点单页面展示）
 * 查询用户当前 BOOKED 状态的预约，返回门店、桌位、时间等信息
 */
public class CurrentReservationVO {
    private String orderId;          // ORD + reservationId 格式
    private Integer storeId;
    private String storeName;
    private Integer tableId;
    private String tableName;
    private String tableType;
    private Integer tableCapacity;
    private String catTheme;
    private String catName;
    private String reserveDate;      // yyyy-MM-dd
    private String reserveTime;      // HH:mm
    private Integer duration;        // 小时
    private Integer persons;         // 人数
    private String remark;           // specialRequest

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public Integer getStoreId() { return storeId; }
    public void setStoreId(Integer storeId) { this.storeId = storeId; }
    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }
    public Integer getTableId() { return tableId; }
    public void setTableId(Integer tableId) { this.tableId = tableId; }
    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }
    public String getTableType() { return tableType; }
    public void setTableType(String tableType) { this.tableType = tableType; }
    public Integer getTableCapacity() { return tableCapacity; }
    public void setTableCapacity(Integer tableCapacity) { this.tableCapacity = tableCapacity; }
    public String getCatTheme() { return catTheme; }
    public void setCatTheme(String catTheme) { this.catTheme = catTheme; }
    public String getCatName() { return catName; }
    public void setCatName(String catName) { this.catName = catName; }
    public String getReserveDate() { return reserveDate; }
    public void setReserveDate(String reserveDate) { this.reserveDate = reserveDate; }
    public String getReserveTime() { return reserveTime; }
    public void setReserveTime(String reserveTime) { this.reserveTime = reserveTime; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public Integer getPersons() { return persons; }
    public void setPersons(Integer persons) { this.persons = persons; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
