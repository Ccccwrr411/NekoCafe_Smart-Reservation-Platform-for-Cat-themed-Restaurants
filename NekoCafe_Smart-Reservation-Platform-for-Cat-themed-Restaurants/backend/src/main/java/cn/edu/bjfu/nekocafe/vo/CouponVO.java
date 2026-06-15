package cn.edu.bjfu.nekocafe.vo;

/**
 * VO - 优惠券响应体（对应接口 G-1 / G-2）
 * entity UserCoupons + Promotions 联查组装
 */
public class CouponVO {
    private String id;
    private String name;
    private String type;         // discount / cashback / freebie
    private Double value;        // 折扣率或金额
    private Integer maxDiscount;
    private Integer minAmount;
    private String expireDate;
    private String status;       // unused / used / expired
    private Boolean stackable;
    private String ruleId;
    private Integer saving;      // G-2 可用优惠券额外字段：预计节省金额

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }
    public Integer getMaxDiscount() { return maxDiscount; }
    public void setMaxDiscount(Integer maxDiscount) { this.maxDiscount = maxDiscount; }
    public Integer getMinAmount() { return minAmount; }
    public void setMinAmount(Integer minAmount) { this.minAmount = minAmount; }
    public String getExpireDate() { return expireDate; }
    public void setExpireDate(String expireDate) { this.expireDate = expireDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Boolean getStackable() { return stackable; }
    public void setStackable(Boolean stackable) { this.stackable = stackable; }
    public String getRuleId() { return ruleId; }
    public void setRuleId(String ruleId) { this.ruleId = ruleId; }
    public Integer getSaving() { return saving; }
    public void setSaving(Integer saving) { this.saving = saving; }
}
