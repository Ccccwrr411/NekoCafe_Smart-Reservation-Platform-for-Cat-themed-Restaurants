package cn.edu.bjfu.nekocafe.entity;

import java.util.Date;

public class Promotions {
    private Integer promoId;

    private String name;

    private String type;

    private Object ruleJson;

    private Date startTime;

    private Date endTime;

    private Object applicableStores;

    private Boolean isActive;

    public Integer getPromoId() {
        return promoId;
    }

    public void setPromoId(Integer promoId) {
        this.promoId = promoId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getRuleJson() {
        return ruleJson;
    }

    public void setRuleJson(Object ruleJson) {
        this.ruleJson = ruleJson;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Object getApplicableStores() {
        return applicableStores;
    }

    public void setApplicableStores(Object applicableStores) {
        this.applicableStores = applicableStores;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}