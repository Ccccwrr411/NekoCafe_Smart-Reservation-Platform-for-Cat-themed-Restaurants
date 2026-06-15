package cn.edu.bjfu.nekocafe.vo;

/**
 * 总部运营 · 单门店卡片 VO
 *
 * 字段与 dashboard.wxml 全部门店概览 UI 一一对应：
 * hqOverview.stores[i].{ name, status, manager, managerPhone, rating,
 *   todayRevenue, todayOrders, tableCount, availableTables, occupancyRate }
 */
public class StoreCardVO {

    private Integer id;
    private String name;
    private String status;          // "open" | "closed"
    private String manager;
    private String managerPhone;
    private Double rating;
    private Integer todayRevenue;
    private Integer todayOrders;
    private Integer tableCount;
    private Integer availableTables;
    private Double occupancyRate;     // 0.0 ~ 1.0

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getManager() {
        return manager;
    }
    public void setManager(String manager) {
        this.manager = manager;
    }
    public String getManagerPhone() {
        return managerPhone;
    }
    public void setManagerPhone(String managerPhone) {
        this.managerPhone = managerPhone;
    }
    public Double getRating() {
        return rating;
    }
    public void setRating(Double rating) {
        this.rating = rating;
    }
    public Integer getTodayRevenue() {
        return todayRevenue;
    }
    public void setTodayRevenue(Integer todayRevenue) {
        this.todayRevenue = todayRevenue;
    }
    public Integer getTodayOrders() {
        return todayOrders;
    }
    public void setTodayOrders(Integer todayOrders) {
        this.todayOrders = todayOrders;
    }
    public Integer getTableCount() {
        return tableCount;
    }
    public void setTableCount(Integer tableCount) {
        this.tableCount = tableCount;
    }
    public Integer getAvailableTables() {
        return availableTables;
    }
    public void setAvailableTables(Integer availableTables) {
        this.availableTables = availableTables;
    }
    public Double getOccupancyRate() {
        return occupancyRate;
    }
    public void setOccupancyRate(Double occupancyRate) {
        this.occupancyRate = occupancyRate;
    }
}
