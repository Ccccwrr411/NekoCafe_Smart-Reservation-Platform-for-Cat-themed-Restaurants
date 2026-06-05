package cn.edu.bjfu.nekocafe.vo;

import java.util.List;

/**
 * VO - 数据看板响应体（对应接口 K-1）
 */
public class DashboardMetricsVO {
    private Integer storeId;
    private String range;
    private TrendVO spaceEfficiency;
    private TrendVO turnoverRate;
    private TrendVO repurchaseRate;
    private TodayOverviewVO todayOverview;

    public static class TrendVO {
        private List<String> labels;
        private List<Double> values;
        public List<String> getLabels() { return labels; }
        public void setLabels(List<String> labels) { this.labels = labels; }
        public List<Double> getValues() { return values; }
        public void setValues(List<Double> values) { this.values = values; }
    }

    public static class TodayOverviewVO {
        private Integer revenue;
        private Integer orderCount;
        private Integer newMembers;
        private Integer avgOrderValue;
        public Integer getRevenue() { return revenue; }
        public void setRevenue(Integer revenue) { this.revenue = revenue; }
        public Integer getOrderCount() { return orderCount; }
        public void setOrderCount(Integer orderCount) { this.orderCount = orderCount; }
        public Integer getNewMembers() { return newMembers; }
        public void setNewMembers(Integer newMembers) { this.newMembers = newMembers; }
        public Integer getAvgOrderValue() { return avgOrderValue; }
        public void setAvgOrderValue(Integer avgOrderValue) { this.avgOrderValue = avgOrderValue; }
    }

    public Integer getStoreId() { return storeId; }
    public void setStoreId(Integer storeId) { this.storeId = storeId; }
    public String getRange() { return range; }
    public void setRange(String range) { this.range = range; }
    public TrendVO getSpaceEfficiency() { return spaceEfficiency; }
    public void setSpaceEfficiency(TrendVO spaceEfficiency) { this.spaceEfficiency = spaceEfficiency; }
    public TrendVO getTurnoverRate() { return turnoverRate; }
    public void setTurnoverRate(TrendVO turnoverRate) { this.turnoverRate = turnoverRate; }
    public TrendVO getRepurchaseRate() { return repurchaseRate; }
    public void setRepurchaseRate(TrendVO repurchaseRate) { this.repurchaseRate = repurchaseRate; }
    public TodayOverviewVO getTodayOverview() { return todayOverview; }
    public void setTodayOverview(TodayOverviewVO todayOverview) { this.todayOverview = todayOverview; }
}
