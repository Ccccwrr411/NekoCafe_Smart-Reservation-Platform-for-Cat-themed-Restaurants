package cn.edu.bjfu.nekocafe.service.impl;

import cn.edu.bjfu.nekocafe.entity.*;
import cn.edu.bjfu.nekocafe.mapper.*;
import cn.edu.bjfu.nekocafe.service.DashboardService;
import cn.edu.bjfu.nekocafe.vo.DashboardMetricsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class DashboardServiceImpl implements DashboardService {
    @Autowired private StoreDailyStatsMapper storeDailyStatsMapper;
    @Autowired private ReservationsMapper reservationsMapper;
    @Autowired private UsersMapper usersMapper;
    @Autowired private MemberExtMapper memberExtMapper;

    @Override
    public DashboardMetricsVO getMetrics(Integer storeId, String range) {
        int days = 7;
        if (range != null && range.matches("\\d+d"))
            days = Integer.parseInt(range.replace("d", ""));

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59);
        Date today = cal.getTime();
        cal.add(Calendar.DAY_OF_YEAR, -days);
        Date startDate = cal.getTime();

        // 1. 从 StoreDailyStats 表查询历史数据
        StoreDailyStatsExample statsEx = new StoreDailyStatsExample();
        statsEx.setOrderByClause("stat_date ASC");
        statsEx.createCriteria()
            .andStoreIdEqualTo(storeId)
            .andStatDateGreaterThanOrEqualTo(startDate)
            .andStatDateLessThanOrEqualTo(today);
        List<StoreDailyStats> statsList = storeDailyStatsMapper.selectByExample(statsEx);

        SimpleDateFormat labelFmt = new SimpleDateFormat("MM/dd");
        List<String> labels = new ArrayList<>();
        List<Double> revenuePerSeatList = new ArrayList<>();
        List<Double> turnoverRateList = new ArrayList<>();
        List<Double> repurchaseRateList = new ArrayList<>();

        if (statsList != null) {
            for (StoreDailyStats s : statsList) {
                labels.add(labelFmt.format(s.getStatDate()));
                revenuePerSeatList.add(s.getRevenuePerSeat() != null ? s.getRevenuePerSeat().doubleValue() : 0.0);
                turnoverRateList.add(s.getTableTurnoverRate() != null ? s.getTableTurnoverRate().doubleValue() : 0.0);
                // repurchaseRate = repeatCustomers / totalReservations * 100
                double rr = 0.0;
                if (s.getTotalReservations() != null && s.getTotalReservations() > 0
                    && s.getRepeatCustomers() != null) {
                    rr = s.getRepeatCustomers().doubleValue() / s.getTotalReservations() * 100.0;
                }
                repurchaseRateList.add(Math.round(rr * 10.0) / 10.0);
            }
        }

        // 2. 今日概览 — 从 reservations 表实时统计
        Calendar todayStart = Calendar.getInstance();
        todayStart.set(Calendar.HOUR_OF_DAY, 0); todayStart.set(Calendar.MINUTE, 0);
        todayStart.set(Calendar.SECOND, 0); todayStart.set(Calendar.MILLISECOND, 0);
        Date today0 = todayStart.getTime();

        Calendar todayEnd = Calendar.getInstance();
        todayEnd.set(Calendar.HOUR_OF_DAY, 23); todayEnd.set(Calendar.MINUTE, 59);
        todayEnd.set(Calendar.SECOND, 59); todayEnd.set(Calendar.MILLISECOND, 999);
        Date today24 = todayEnd.getTime();

        // 查询今日完成的预约订单
        ReservationsExample resEx = new ReservationsExample();
        resEx.createCriteria()
            .andStoreIdEqualTo(storeId)
            .andCreatedAtBetween(today0, today24);
        List<Reservations> todayRes = reservationsMapper.selectByExample(resEx);

        int revenue = 0, orderCount = 0;
        if (todayRes != null) {
            orderCount = todayRes.size();
            for (Reservations r : todayRes) {
                if (r.getTotalAmount() != null) revenue += r.getTotalAmount().intValue();
            }
        }
        int avgOrderValue = orderCount > 0 ? revenue / orderCount : 0;

        // 新会员：今日创建的 users
        UsersExample userEx = new UsersExample();
        userEx.createCriteria().andCreatedAtBetween(today0, today24);
        long newMembers = usersMapper.countByExample(userEx);

        // 3. 组装 VO
        DashboardMetricsVO.TodayOverviewVO overview = new DashboardMetricsVO.TodayOverviewVO();
        overview.setRevenue(revenue);
        overview.setOrderCount(orderCount);
        overview.setNewMembers((int) newMembers);
        overview.setAvgOrderValue(avgOrderValue);

        DashboardMetricsVO.ChartDataVO chart = new DashboardMetricsVO.ChartDataVO();
        chart.setLabels(labels);
        chart.setRevenuePerSeat(revenuePerSeatList);
        chart.setTableTurnoverRate(turnoverRateList);
        chart.setRepurchaseRate(repurchaseRateList);

        DashboardMetricsVO vo = new DashboardMetricsVO();
        vo.setStoreId(storeId);
        vo.setRange(range);
        vo.setTodayOverview(overview);
        vo.setChartData(chart);
        return vo;
    }
}
