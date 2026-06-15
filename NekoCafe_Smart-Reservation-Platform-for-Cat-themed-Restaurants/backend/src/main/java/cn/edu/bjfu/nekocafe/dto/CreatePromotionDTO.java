package cn.edu.bjfu.nekocafe.dto;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 总部运营 · 创建活动 DTO
 *
 * ruleJson 示例（按 type 区分）：
 *   DISCOUNT: {"discount": 0.8, "max_discount": 20, "min_spend": 0, "stackable": true}
 *   VOUCHER:  {"reduction": 20, "min_spend": 100, "stackable": false}
 */
public class CreatePromotionDTO {

    private String name;
    private String type;                       // DISCOUNT | VOUCHER
    private Map<String, Object> ruleJson;
    private Date startTime;
    private Date endTime;
    private List<Integer> applicableStores;
    private Boolean isActive;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Map<String, Object> getRuleJson() { return ruleJson; }
    public void setRuleJson(Map<String, Object> ruleJson) { this.ruleJson = ruleJson; }
    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }
    public Date getEndTime() { return endTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }
    public List<Integer> getApplicableStores() { return applicableStores; }
    public void setApplicableStores(List<Integer> applicableStores) { this.applicableStores = applicableStores; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
