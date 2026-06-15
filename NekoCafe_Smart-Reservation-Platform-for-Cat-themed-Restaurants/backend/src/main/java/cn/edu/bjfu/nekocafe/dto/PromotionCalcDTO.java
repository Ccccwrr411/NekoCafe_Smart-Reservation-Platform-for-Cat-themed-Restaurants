package cn.edu.bjfu.nekocafe.dto;

import java.util.List;

/**
 * DTO - 优惠试算请求体（对应接口 G-4）
 */
public class PromotionCalcDTO {
    private Integer storeId;
    private Integer amount;
    private List<String> couponIds;

    public Integer getStoreId() { return storeId; }
    public void setStoreId(Integer storeId) { this.storeId = storeId; }
    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }
    public List<String> getCouponIds() { return couponIds; }
    public void setCouponIds(List<String> couponIds) { this.couponIds = couponIds; }
}
