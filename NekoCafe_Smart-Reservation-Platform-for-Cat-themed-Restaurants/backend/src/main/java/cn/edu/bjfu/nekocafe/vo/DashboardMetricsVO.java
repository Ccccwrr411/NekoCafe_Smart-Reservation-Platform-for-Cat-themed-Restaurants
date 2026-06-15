package cn.edu.bjfu.nekocafe.vo;

import java.util.List;

/**
 * VO - 数据看板响应体（对应接口 K-1）
 * 结构与前端 staffDashboard 页面字段一一对应：
 *   metrics.todayOverview.revenue / orderCount / newMembers / avgOrderValue
 *   metrics.chartData.labels / revenuePerSeat / tableTurnoverRate / repurchaseRate
 */
public class DashboardMetricsVO {
    private Integer storeId;
    private String range;
    private ChartDataVO chartData;
    private TodayOverviewVO todayOverview;

    /**
     * 图表数据（前端通过 metrics.chartData 访问）
     */
    public static class ChartDataVO {
        private List<String> labels;
        private List<Double> revenuePerSeat;
        private List<Double> tableTurnoverRate;
        private List<Double> repurchaseRate;

        public List<String> getLabels() { return labels; }
        public void setLabels(List<String> labels) { this.labels = labels; }
        public List<Double> getRevenuePerSeat() { return revenuePerSeat; }
        public void setRevenuePerSeat(List<Double> revenuePerSeat) { this.revenuePerSeat = revenuePerSeat; }
        public List<Double> getTableTurnoverRate() { return tableTurnoverRate; }
        public void setTableTurnoverRate(List<Double> tableTurnoverRate) { this.tableTurnoverRate = tableTurnoverRate; }
        public List<Double> getRepurchaseRate() { return repurchaseRate; }
        public void setRepurchaseRate(List<Double> repurchaseRate) { this.repurchaseRate = repurchaseRate; }
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
    public ChartDataVO getChartData() { return chartData; }
    public void setChartData(ChartDataVO chartData) { this.chartData = chartData; }
    public TodayOverviewVO getTodayOverview() { return todayOverview; }
    public void setTodayOverview(TodayOverviewVO todayOverview) { this.todayOverview = todayOverview; }
}