package cn.edu.bjfu.nekocafe.entity;

import java.math.BigDecimal;

public class StoreDailyStats extends StoreDailyStatsKey {
    private Integer totalReservations;

    private BigDecimal totalRevenue;

    private BigDecimal tableTurnoverRate;

    private BigDecimal revenuePerSeat;

    private Integer repeatCustomers;

    public Integer getTotalReservations() {
        return totalReservations;
    }

    public void setTotalReservations(Integer totalReservations) {
        this.totalReservations = totalReservations;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public BigDecimal getTableTurnoverRate() {
        return tableTurnoverRate;
    }

    public void setTableTurnoverRate(BigDecimal tableTurnoverRate) {
        this.tableTurnoverRate = tableTurnoverRate;
    }

    public BigDecimal getRevenuePerSeat() {
        return revenuePerSeat;
    }

    public void setRevenuePerSeat(BigDecimal revenuePerSeat) {
        this.revenuePerSeat = revenuePerSeat;
    }

    public Integer getRepeatCustomers() {
        return repeatCustomers;
    }

    public void setRepeatCustomers(Integer repeatCustomers) {
        this.repeatCustomers = repeatCustomers;
    }
}