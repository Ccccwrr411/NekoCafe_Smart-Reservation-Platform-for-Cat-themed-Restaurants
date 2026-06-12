package cn.edu.bjfu.nekocafe.vo;

import java.util.List;

/**
 * VO - 订单列表 & 详情响应体（对应接口 E-1 / E-3）
 * entity Reservations + OrderItems
 */
public class OrderVO {
    private String id;           // ORD + reservationId 格式
    private Integer storeId;
    private String storeName;
    private Integer tableId;
    private String tableName;
    private String reserveDate;  // yyyy-MM-dd
    private String reserveTime;  // HH:mm
    private Integer duration;    // 小时（durationMin / 60）
    private Integer persons;     // partySize
    private String status;       // pending/confirmed/completed/cancelled/refunding/refunded
    private Integer totalAmount;
    private Integer discountAmount;
    private Integer finalAmount;
    private String createTime;
    private String payTime;
    private String payType;
    private String remark;       // specialRequest
    private List<OrderItemVO> items;
    private List<TimelineVO> timeline;
    private Boolean canCancel;
    private Boolean canReschedule;
    private Boolean canRefund;
    private Boolean hasReview;

    /** 菜品明细 */
    public static class OrderItemVO {
        private String name;
        private Integer qty;
        private Integer price;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getQty() { return qty; }
        public void setQty(Integer qty) { this.qty = qty; }
        public Integer getPrice() { return price; }
        public void setPrice(Integer price) { this.price = price; }
    }

    /** 时间线节点 */
    public static class TimelineVO {
        private String time;
        private String title;
        private String desc;

        public String getTime() { return time; }
        public void setTime(String time) { this.time = time; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDesc() { return desc; }
        public void setDesc(String desc) { this.desc = desc; }
    }

    // ---- Getters & Setters ----
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Integer getStoreId() { return storeId; }
    public void setStoreId(Integer storeId) { this.storeId = storeId; }
    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }
    public Integer getTableId() { return tableId; }
    public void setTableId(Integer tableId) { this.tableId = tableId; }
    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }
    public String getReserveDate() { return reserveDate; }
    public void setReserveDate(String reserveDate) { this.reserveDate = reserveDate; }
    public String getReserveTime() { return reserveTime; }
    public void setReserveTime(String reserveTime) { this.reserveTime = reserveTime; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public Integer getPersons() { return persons; }
    public void setPersons(Integer persons) { this.persons = persons; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Integer totalAmount) { this.totalAmount = totalAmount; }
    public Integer getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(Integer discountAmount) { this.discountAmount = discountAmount; }
    public Integer getFinalAmount() { return finalAmount; }
    public void setFinalAmount(Integer finalAmount) { this.finalAmount = finalAmount; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
    public String getPayTime() { return payTime; }
    public void setPayTime(String payTime) { this.payTime = payTime; }
    public String getPayType() { return payType; }
    public void setPayType(String payType) { this.payType = payType; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public List<OrderItemVO> getItems() { return items; }
    public void setItems(List<OrderItemVO> items) { this.items = items; }
    public List<TimelineVO> getTimeline() { return timeline; }
    public void setTimeline(List<TimelineVO> timeline) { this.timeline = timeline; }
    public Boolean getCanCancel() { return canCancel; }
    public void setCanCancel(Boolean canCancel) { this.canCancel = canCancel; }
    public Boolean getCanReschedule() { return canReschedule; }
    public void setCanReschedule(Boolean canReschedule) { this.canReschedule = canReschedule; }
    public Boolean getCanRefund() { return canRefund; }
    public void setCanRefund(Boolean canRefund) { this.canRefund = canRefund; }
    public Boolean getHasReview() { return hasReview; }
    public void setHasReview(Boolean hasReview) { this.hasReview = hasReview; }
}
