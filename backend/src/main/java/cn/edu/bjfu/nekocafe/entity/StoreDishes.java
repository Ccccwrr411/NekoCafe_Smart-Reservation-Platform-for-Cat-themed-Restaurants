package cn.edu.bjfu.nekocafe.entity;

import java.math.BigDecimal;

public class StoreDishes extends StoreDishesKey {
    private BigDecimal priceOverride;

    private Boolean isAvailable;

    public BigDecimal getPriceOverride() {
        return priceOverride;
    }

    public void setPriceOverride(BigDecimal priceOverride) {
        this.priceOverride = priceOverride;
    }

    public Boolean getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }
}