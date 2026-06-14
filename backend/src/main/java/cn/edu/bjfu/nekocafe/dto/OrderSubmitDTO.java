package cn.edu.bjfu.nekocafe.dto;

import java.util.List;

/**
 * DTO - 提交订单请求体（对应接口 E-2）
 */
public class OrderSubmitDTO {
    private Integer storeId;
    private List<OrderItemDTO> items;
    private Integer totalAmount;
    private Integer finalAmount;
    private Integer discount;
    private List<String> couponIds;
    private String remark;
    private Integer tableId;
    private String orderId;

    public static class OrderItemDTO {
        private Integer menuId;
        private String name;
        private Integer price;
        private Integer qty;

        public Integer getMenuId() { return menuId; }
        public void setMenuId(Integer menuId) { this.menuId = menuId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getPrice() { return price; }
        public void setPrice(Integer price) { this.price = price; }
        public Integer getQty() { return qty; }
        public void setQty(Integer qty) { this.qty = qty; }
    }

    public Integer getStoreId() { return storeId; }
    public void setStoreId(Integer storeId) { this.storeId = storeId; }
    public List<OrderItemDTO> getItems() { return items; }
    public void setItems(List<OrderItemDTO> items) { this.items = items; }
    public Integer getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Integer totalAmount) { this.totalAmount = totalAmount; }
    public Integer getFinalAmount() { return finalAmount; }
    public void setFinalAmount(Integer finalAmount) { this.finalAmount = finalAmount; }
    public Integer getDiscount() { return discount; }
    public void setDiscount(Integer discount) { this.discount = discount; }
    public List<String> getCouponIds() { return couponIds; }
    public void setCouponIds(List<String> couponIds) { this.couponIds = couponIds; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Integer getTableId() { return tableId; }
    public void setTableId(Integer tableId) { this.tableId = tableId; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
}
