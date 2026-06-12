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

    // 前端展示用
    private String statusIcon;
    private String statusDesc;

    // ===== 扩展字段（E-3 详情） =====

    // stores 扩展
    private String storeAddress;
    private String storePhone;

    // tables 扩展
    private Integer tableCapacity;
    private String tableType;
    private String catTheme;

    // table_status
    private String tableStatus;

    // payments 扩展
    private String paymentMethod;
    private String transactionId;
    private String paymentStatus;

    // refund_records
    private String refundReason;
    private String refundStatus;
    private String refundCreatedAt;
    private String refundCompletedAt;

    // coupon 汇总
    private List<CouponVO> coupons;
    private Integer couponDiscount;

    // member_ext
    private Integer memberLevel;
    private Integer memberPoints;

    // users
    private String customerName;
    private String customerPhone;
    private String customerAvatar;

    /** 菜品明细 */
    public static class OrderItemVO {
        private String name;
        private Integer qty;
        private Integer price;
        private String category;
        private String imageUrl;
        private String description;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getQty() { return qty; }
        public void setQty(Integer qty) { this.qty = qty; }
        public Integer getPrice() { return price; }
        public void setPrice(Integer price) { this.price = price; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
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

    /** 优惠券信息 */
    public static class CouponVO {
        private Long couponId;
        private String name;
        private String type;
        private Integer discountAmount;
        private String status;

        public Long getCouponId() { return couponId; }
        public void setCouponId(Long couponId) { this.couponId = couponId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public Integer getDiscountAmount() { return discountAmount; }
        public void setDiscountAmount(Integer discountAmount) { this.discountAmount = discountAmount; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
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
    public String getStatusIcon() { return statusIcon; }
    public void setStatusIcon(String statusIcon) { this.statusIcon = statusIcon; }
    public String getStatusDesc() { return statusDesc; }
    public void setStatusDesc(String statusDesc) { this.statusDesc = statusDesc; }

    // 扩展字段 getters/setters
    public String getStoreAddress() { return storeAddress; }
    public void setStoreAddress(String storeAddress) { this.storeAddress = storeAddress; }
    public String getStorePhone() { return storePhone; }
    public void setStorePhone(String storePhone) { this.storePhone = storePhone; }
    public Integer getTableCapacity() { return tableCapacity; }
    public void setTableCapacity(Integer tableCapacity) { this.tableCapacity = tableCapacity; }
    public String getTableType() { return tableType; }
    public void setTableType(String tableType) { this.tableType = tableType; }
    public String getCatTheme() { return catTheme; }
    public void setCatTheme(String catTheme) { this.catTheme = catTheme; }
    public String getTableStatus() { return tableStatus; }
    public void setTableStatus(String tableStatus) { this.tableStatus = tableStatus; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public String getRefundReason() { return refundReason; }
    public void setRefundReason(String refundReason) { this.refundReason = refundReason; }
    public String getRefundStatus() { return refundStatus; }
    public void setRefundStatus(String refundStatus) { this.refundStatus = refundStatus; }
    public String getRefundCreatedAt() { return refundCreatedAt; }
    public void setRefundCreatedAt(String refundCreatedAt) { this.refundCreatedAt = refundCreatedAt; }
    public String getRefundCompletedAt() { return refundCompletedAt; }
    public void setRefundCompletedAt(String refundCompletedAt) { this.refundCompletedAt = refundCompletedAt; }
    public List<CouponVO> getCoupons() { return coupons; }
    public void setCoupons(List<CouponVO> coupons) { this.coupons = coupons; }
    public Integer getCouponDiscount() { return couponDiscount; }
    public void setCouponDiscount(Integer couponDiscount) { this.couponDiscount = couponDiscount; }
    public Integer getMemberLevel() { return memberLevel; }
    public void setMemberLevel(Integer memberLevel) { this.memberLevel = memberLevel; }
    public Integer getMemberPoints() { return memberPoints; }
    public void setMemberPoints(Integer memberPoints) { this.memberPoints = memberPoints; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public String getCustomerAvatar() { return customerAvatar; }
    public void setCustomerAvatar(String customerAvatar) { this.customerAvatar = customerAvatar; }
}
