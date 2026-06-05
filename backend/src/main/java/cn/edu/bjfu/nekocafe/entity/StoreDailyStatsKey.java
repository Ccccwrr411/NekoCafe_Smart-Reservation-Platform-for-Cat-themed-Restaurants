package cn.edu.bjfu.nekocafe.entity;

import java.util.Date;

public class StoreDailyStatsKey {
    private Date statDate;

    private Integer storeId;

    public Date getStatDate() {
        return statDate;
    }

    public void setStatDate(Date statDate) {
        this.statDate = statDate;
    }

    public Integer getStoreId() {
        return storeId;
    }

    public void setStoreId(Integer storeId) {
        this.storeId = storeId;
    }
}