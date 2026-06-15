package cn.edu.bjfu.nekocafe.vo;

import java.util.List;

/**
 * 总部运营 · 全部门店概览 VO
 */
public class StoresOverviewVO {

    private Integer totalRevenue;
    private Integer totalOrders;
    private Integer totalMembers;
    private Double avgDailyTurnover;
    private List<StoreCardVO> stores;

    public Integer getTotalRevenue() {
        return totalRevenue;
    }
    public void setTotalRevenue(Integer totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
    public Integer getTotalOrders() {
        return totalOrders;
    }
    public void setTotalOrders(Integer totalOrders) {
        this.totalOrders = totalOrders;
    }
    public Integer getTotalMembers() {
        return totalMembers;
    }
    public void setTotalMembers(Integer totalMembers) {
        this.totalMembers = totalMembers;
    }
    public Double getAvgDailyTurnover() {
        return avgDailyTurnover;
    }
    public void setAvgDailyTurnover(Double avgDailyTurnover) {
        this.avgDailyTurnover = avgDailyTurnover;
    }
    public List<StoreCardVO> getStores() {
        return stores;
    }
    public void setStores(List<StoreCardVO> stores) {
        this.stores = stores;
    }
}
