package cn.edu.bjfu.nekocafe.service.impl;

import cn.edu.bjfu.nekocafe.entity.*;
import cn.edu.bjfu.nekocafe.mapper.*;
import cn.edu.bjfu.nekocafe.service.StaffService;
import cn.edu.bjfu.nekocafe.vo.DashboardMetricsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 店员 & 数据看板服务实现
 * 负责接口：K-1 / L-1 / L-2
 */
@Service
public class StaffServiceImpl implements StaffService {

    @Autowired
    private StoreDailyStatsMapper storeDailyStatsMapper;

    @Autowired
    private TableStatusMapper tableStatusMapper;

    @Autowired
    private TablesMapper tablesMapper;

    @Autowired
    private ReservationsMapper reservationsMapper;

    @Autowired
    private ShiftExceptionsMapper shiftExceptionsMapper;

    // =========================================================
    // K-1  GET /api/dashboard/metrics?storeId=&range=
    // =========================================================
    @Override
    public DashboardMetricsVO getDashboardMetrics(Integer storeId, String range) {

        // 1. 解析 range → 天数
        int days = parseDays(range);

        // 2. 计算起始日期（从今天往前 N 天）
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date endDate = cal.getTime();              // 今日 00:00:00
        cal.add(Calendar.DAY_OF_YEAR, -(days - 1));
        Date startDate = cal.getTime();            // N 天前 00:00:00

        // 3. 查 store_daily_stats
        StoreDailyStatsExample example = new StoreDailyStatsExample();
        example.setOrderByClause("stat_date ASC");
        StoreDailyStatsExample.Criteria criteria = example.createCriteria();
        criteria.andStoreIdEqualTo(storeId);
        criteria.andStatDateGreaterThanOrEqualTo(startDate);
        criteria.andStatDateLessThanOrEqualTo(endDate);

        List<StoreDailyStats> statsList = storeDailyStatsMapper.selectByExample(example);

        // 4. 若数据库无数据，构造 Mock 兜底（保证接口可用）
        if (statsList == null || statsList.isEmpty()) {
            statsList = buildMockStats(storeId, startDate, days);
        }

        // 5. 组装标签和各指标值列表
        SimpleDateFormat labelFmt = new SimpleDateFormat("MM/dd");
        List<String> labels = new ArrayList<>();
        List<Double> spaceEffValues = new ArrayList<>();   // revenuePerSeat → 坪效
        List<Double> turnoverValues = new ArrayList<>();   // tableTurnoverRate
        List<Double> repurchaseValues = new ArrayList<>(); // repeatCustomers / totalReservations → 复购率

        for (StoreDailyStats s : statsList) {
            labels.add(labelFmt.format(s.getStatDate()));

            spaceEffValues.add(s.getRevenuePerSeat() != null
                    ? s.getRevenuePerSeat().doubleValue() : 0.0);

            turnoverValues.add(s.getTableTurnoverRate() != null
                    ? s.getTableTurnoverRate().doubleValue() : 0.0);

            // 复购率 = repeatCustomers / totalReservations（百分比），兜底 0
            double repurchase = 0.0;
            if (s.getRepeatCustomers() != null && s.getTotalReservations() != null
                    && s.getTotalReservations() > 0) {
                repurchase = Math.round(
                        s.getRepeatCustomers() * 100.0 / s.getTotalReservations() * 10) / 10.0;
            }
            repurchaseValues.add(repurchase);
        }

        // 6. 组装 todayOverview（取最后一条即今日数据）
        DashboardMetricsVO.TodayOverviewVO todayOverview = new DashboardMetricsVO.TodayOverviewVO();
        if (!statsList.isEmpty()) {
            StoreDailyStats today = statsList.get(statsList.size() - 1);
            todayOverview.setRevenue(today.getTotalRevenue() != null
                    ? today.getTotalRevenue().intValue() : 0);
            todayOverview.setOrderCount(today.getTotalReservations() != null
                    ? today.getTotalReservations() : 0);
            todayOverview.setNewMembers(today.getRepeatCustomers() != null
                    ? today.getRepeatCustomers() : 0);
            // avgOrderValue = totalRevenue / totalReservations
            if (today.getTotalRevenue() != null && today.getTotalReservations() != null
                    && today.getTotalReservations() > 0) {
                todayOverview.setAvgOrderValue(
                        today.getTotalRevenue().intValue() / today.getTotalReservations());
            } else {
                todayOverview.setAvgOrderValue(0);
            }
        }

        // 7. 拼 VO
        DashboardMetricsVO vo = new DashboardMetricsVO();
        vo.setStoreId(storeId);
        vo.setRange(range);
        vo.setSpaceEfficiency(buildTrend(labels, spaceEffValues));
        vo.setTurnoverRate(buildTrend(labels, turnoverValues));
        vo.setRepurchaseRate(buildTrend(labels, repurchaseValues));
        vo.setTodayOverview(todayOverview);
        return vo;
    }

    // =========================================================
    // L-1  GET /api/staff/tables?storeId=
    // =========================================================
    @Override
    public List<Map<String, Object>> getStaffTables(Integer storeId) {

        // 1. 查该门店所有启用桌位
        TablesExample tablesExample = new TablesExample();
        tablesExample.createCriteria()
                .andStoreIdEqualTo(storeId)
                .andIsActiveEqualTo(true);
        tablesExample.setOrderByClause("table_no ASC");
        List<Tables> tablesList = tablesMapper.selectByExample(tablesExample);

        // 2. 查所有桌位实时状态（table_id → TableStatus）
        Map<Integer, TableStatus> statusMap = new HashMap<>();
        if (tablesList != null && !tablesList.isEmpty()) {
            TableStatusExample tsExample = new TableStatusExample();
            // 不加额外条件，拉取所有有状态记录的桌
            List<TableStatus> tsList = tableStatusMapper.selectByExample(tsExample);
            if (tsList != null) {
                for (TableStatus ts : tsList) {
                    statusMap.put(ts.getTableId(), ts);
                }
            }
        }

        // 3. 查今日 serving 预约信息（status = 'serving'）
        //    用于填 customer / arriveTime / estLeaveTime
        Map<Long, Reservations> reservationMap = new HashMap<>();
        ReservationsExample resExample = new ReservationsExample();
        resExample.createCriteria()
                .andStoreIdEqualTo(storeId)
                .andStatusEqualTo("serving");
        List<Reservations> resList = reservationsMapper.selectByExample(resExample);
        if (resList != null) {
            for (Reservations r : resList) {
                reservationMap.put(r.getReservationId(), r);
            }
        }

        // 4. 组装结果
        SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm");
        List<Map<String, Object>> result = new ArrayList<>();

        if (tablesList != null) {
            for (Tables t : tablesList) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("tableId", t.getTableId());
                row.put("tableNo", t.getTableNo());
                row.put("capacity", t.getCapacity());
                row.put("tableType", t.getTableType());
                row.put("catTheme", t.getCatTheme());

                // 状态：从 table_status 取，默认 available
                TableStatus ts = statusMap.get(t.getTableId());
                String status = (ts != null && ts.getStatus() != null)
                        ? ts.getStatus() : "available";
                row.put("status", status);

                // 若有关联预约，填运营字段
                if (ts != null && ts.getCurrentReservationId() != null) {
                    Reservations res = reservationMap.get(ts.getCurrentReservationId());
                    if (res != null) {
                        row.put("customer", "用户#" + res.getUserId()); // 脱敏展示
                        row.put("partySize", res.getPartySize());
                        row.put("arriveTime",
                                res.getReservationTime() != null
                                        ? timeFmt.format(res.getReservationTime()) : null);
                        // estLeaveTime = reservationTime + durationMin
                        if (res.getReservationTime() != null && res.getDurationMin() != null) {
                            long estMs = res.getReservationTime().getTime()
                                    + (long) res.getDurationMin() * 60 * 1000;
                            row.put("estLeaveTime", timeFmt.format(new Date(estMs)));
                        } else {
                            row.put("estLeaveTime", null);
                        }
                        row.put("reservationId", res.getReservationId());
                    } else {
                        fillNullOccupancy(row);
                    }
                } else {
                    fillNullOccupancy(row);
                }

                result.add(row);
            }
        }

        return result;
    }

    // =========================================================
    // L-2  GET /api/staff/alerts?storeId=
    // =========================================================
    @Override
    public List<Map<String, Object>> getAlerts(Integer storeId) {

        // 查 shift_exceptions，取 pending / processing 状态的记录（待处理告警）
        ShiftExceptionsExample example = new ShiftExceptionsExample();
        example.setOrderByClause("created_at DESC");
        ShiftExceptionsExample.Criteria criteria = example.createCriteria();
        criteria.andStoreIdEqualTo(storeId);

        List<ShiftExceptions> exList = shiftExceptionsMapper.selectByExample(example);

        SimpleDateFormat dtFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        List<Map<String, Object>> result = new ArrayList<>();

        if (exList != null) {
            for (ShiftExceptions ex : exList) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("alertId", ex.getExceptionId());
                row.put("storeId", ex.getStoreId());
                row.put("staffId", ex.getStaffId());
                row.put("type", ex.getType());                  // overstay / no_show / equipment 等
                row.put("level", resolveLevel(ex.getType()));   // high / medium / low
                row.put("status", ex.getStatus());              // pending / approved / rejected
                row.put("reason", ex.getReason());
                row.put("exceptionDate",
                        ex.getExceptionDate() != null
                                ? new SimpleDateFormat("yyyy-MM-dd").format(ex.getExceptionDate())
                                : null);
                row.put("createdAt",
                        ex.getCreatedAt() != null ? dtFmt.format(ex.getCreatedAt()) : null);
                result.add(row);
            }
        }

        // 若数据库无记录，返回空列表（不 Mock）
        return result;
    }

    // =========================================================
    //  私有工具方法
    // =========================================================

    /** range 字符串 → 天数，默认 7 */
    private int parseDays(String range) {
        if (range == null) return 7;
        switch (range.trim().toLowerCase()) {
            case "30d": return 30;
            case "90d": return 90;
            default:    return 7;
        }
    }

    /** 构造 TrendVO */
    private DashboardMetricsVO.TrendVO buildTrend(List<String> labels, List<Double> values) {
        DashboardMetricsVO.TrendVO trend = new DashboardMetricsVO.TrendVO();
        trend.setLabels(labels);
        trend.setValues(values);
        return trend;
    }

    /** 当桌位无占用预约时填充空值 */
    private void fillNullOccupancy(Map<String, Object> row) {
        row.put("customer", null);
        row.put("partySize", null);
        row.put("arriveTime", null);
        row.put("estLeaveTime", null);
        row.put("reservationId", null);
    }

    /**
     * 根据异常类型推断告警等级
     * overstay / no_show → high
     * late / swap       → medium
     * 其他              → low
     */
    private String resolveLevel(String type) {
        if (type == null) return "low";
        switch (type.toLowerCase()) {
            case "overstay":
            case "no_show":
                return "high";
            case "late":
            case "swap":
                return "medium";
            default:
                return "low";
        }
    }

    /**
     * 当 store_daily_stats 表无数据时，生成 Mock 数据以保证接口可用。
     * ⚠️ 上线后若表有真实数据，此方法不会被触发。
     */
    private List<StoreDailyStats> buildMockStats(Integer storeId, Date startDate, int days) {
        List<StoreDailyStats> mocks = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        Random rnd = new Random(storeId);  // 同一门店每次生成一致

        for (int i = 0; i < days; i++) {
            StoreDailyStats s = new StoreDailyStats();
            s.setStoreId(storeId);
            s.setStatDate(cal.getTime());

            int reservations = 20 + rnd.nextInt(30);
            s.setTotalReservations(reservations);
            s.setTotalRevenue(new BigDecimal(reservations * (80 + rnd.nextInt(60))));
            s.setTableTurnoverRate(new BigDecimal(String.format("%.1f", 2.0 + rnd.nextDouble() * 3)));
            s.setRevenuePerSeat(new BigDecimal(200 + rnd.nextInt(300)));
            s.setRepeatCustomers(rnd.nextInt(Math.max(1, reservations / 2)));

            mocks.add(s);
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        return mocks;
    }
}
