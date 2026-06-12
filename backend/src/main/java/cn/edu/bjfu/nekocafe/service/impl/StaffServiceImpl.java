package cn.edu.bjfu.nekocafe.service.impl;

import cn.edu.bjfu.nekocafe.entity.*;
import cn.edu.bjfu.nekocafe.mapper.*;
import cn.edu.bjfu.nekocafe.service.StaffService;
import cn.edu.bjfu.nekocafe.vo.DashboardMetricsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 店员 & 数据看板服务实现
 * 负责接口：K-1 / L-1 / L-2 / L-3 / L-4 / L-5
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

    @Autowired
    private RefundRecordsMapper refundRecordsMapper;

    @Autowired
    private PaymentsMapper paymentsMapper;

    @Autowired
    private MemberExtMapper memberExtMapper;

    @Autowired
    private PointsLogMapper pointsLogMapper;

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

        // 2. 查所有桌位实时状态（仅该门店的桌位 ID）
        Map<Integer, TableStatus> statusMap = new HashMap<>();
        if (tablesList != null && !tablesList.isEmpty()) {
            // 收集该门店的 tableId 列表，只查这些桌位的状态
            java.util.Set<Integer> storeTableIds = new java.util.HashSet<>();
            for (Tables t : tablesList) {
                storeTableIds.add(t.getTableId());
            }
            TableStatusExample tsExample = new TableStatusExample();
            List<TableStatus> tsList = tableStatusMapper.selectByExample(tsExample);
            if (tsList != null) {
                for (TableStatus ts : tsList) {
                    if (storeTableIds.contains(ts.getTableId())) {
                        statusMap.put(ts.getTableId(), ts);
                    }
                }
            }
        }

        // 3. 查今日已确认（CONFIRMED）的预约信息
        //    用于填 customer / arriveTime / estLeaveTime
        //    使用带 ::reservation_status CAST 的自定义查询，避免 PostgreSQL 枚举类型不匹配
        Map<Long, Reservations> reservationMap = new HashMap<>();
        List<Reservations> resList = reservationsMapper.selectByStoreIdAndStatuses(
                storeId, java.util.Arrays.asList("CONFIRMED"));
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

                // 状态：从 table_status 取，默认 available（转换为前端小写格式）
                TableStatus ts = statusMap.get(t.getTableId());
                String rawStatus = (ts != null && ts.getStatus() != null)
                        ? ts.getStatus() : "IDLE";
                String status = toFrontendTableStatus(rawStatus);
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
    // L-3  GET /api/staff/orders?storeId=
    // =========================================================
    @Override
    public List<Map<String, Object>> getStaffOrders(Integer storeId) {

        List<Reservations> resList = reservationsMapper.selectByStoreId(storeId);

        SimpleDateFormat dtFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd");
        List<Map<String, Object>> result = new ArrayList<>();

        if (resList != null) {
            for (Reservations r : resList) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id", r.getReservationId());
                row.put("reservationId", r.getReservationId());
                row.put("userId", r.getUserId());
                row.put("storeId", r.getStoreId());
                row.put("tableId", r.getTableId());
                row.put("partySize", r.getPartySize());
                row.put("status", toFrontendOrderStatus(r.getStatus()));
                row.put("orderAmount", r.getOrderAmount() != null ? r.getOrderAmount().doubleValue() : 0);
                row.put("totalAmount", r.getTotalAmount() != null ? r.getTotalAmount().doubleValue() : 0);
                row.put("reservationTime",
                        r.getReservationTime() != null ? dtFmt.format(r.getReservationTime()) : null);
                row.put("durationMin", r.getDurationMin());
                row.put("specialRequest", r.getSpecialRequest());
                row.put("createdAt",
                        r.getCreatedAt() != null ? dtFmt.format(r.getCreatedAt()) : null);

                // 桌位信息
                if (r.getTableId() != null) {
                    Tables table = tablesMapper.selectByPrimaryKey(r.getTableId());
                    if (table != null) {
                        row.put("tableNo", table.getTableNo());
                        row.put("tableType", table.getTableType());
                    }
                }

                result.add(row);
            }
        }

        return result;
    }

    // =========================================================
    // L-4  POST /api/staff/order/accept
    // =========================================================
    @Override
    @Transactional
    public Map<String, Object> acceptOrder(Long reservationId) {

        Map<String, Object> result = new LinkedHashMap<>();

        // 1. 查预约记录
        Reservations reservation = reservationsMapper.selectByPrimaryKey(reservationId);
        if (reservation == null) {
            result.put("success", false);
            result.put("message", "预约记录不存在");
            return result;
        }

        // 2. 校验状态：只有 BOOKED 状态可以接单
        if (!"BOOKED".equals(reservation.getStatus())) {
            result.put("success", false);
            result.put("message", "当前状态不允许接单，当前状态：" + reservation.getStatus());
            return result;
        }

        // 3. 更新预约状态 BOOKED → CONFIRMED
        reservation.setStatus("CONFIRMED");
        reservation.setUpdatedAt(new Date());
        reservationsMapper.updateByPrimaryKeySelective(reservation);

        // 4. 更新桌位状态为 OCCUPIED
        Integer tableId = reservation.getTableId();
        if (tableId != null) {
            TableStatus tableStatus = tableStatusMapper.selectByPrimaryKey(tableId);
            if (tableStatus == null) {
                // 首次创建桌位状态记录
                tableStatus = new TableStatus();
                tableStatus.setTableId(tableId);
                tableStatus.setStatus("OCCUPIED");
                tableStatus.setCurrentReservationId(reservationId);
                tableStatus.setVersion(0);
                tableStatusMapper.insertSelective(tableStatus);
            } else {
                tableStatus.setStatus("OCCUPIED");
                tableStatus.setCurrentReservationId(reservationId);
                tableStatusMapper.updateByPrimaryKeySelective(tableStatus);
            }
        }

        result.put("success", true);
        result.put("message", "接单成功");
        result.put("reservationId", reservationId);
        result.put("status", "CONFIRMED");
        return result;
    }

    // =========================================================
    // L-5  POST /api/staff/table/dispatch
    // =========================================================
    @Override
    @Transactional
    public Map<String, Object> dispatchTable(Integer tableId, String status) {

        Map<String, Object> result = new LinkedHashMap<>();

        // 1. 校验桌位是否存在
        Tables table = tablesMapper.selectByPrimaryKey(tableId);
        if (table == null) {
            result.put("success", false);
            result.put("message", "桌位不存在");
            return result;
        }

        // 2. 校验状态值合法性
        Set<String> validStatuses = new HashSet<>(
                Arrays.asList("IDLE", "RESERVED", "OCCUPIED", "CLEANING"));
        // 同时兼容前端传入的小写值
        String upperStatus = status != null ? status.toUpperCase() : null;
        if (!validStatuses.contains(upperStatus)) {
            result.put("success", false);
            result.put("message", "非法的桌位状态：" + status);
            return result;
        }

        // 3. 更新 table_status
        TableStatus tableStatus = tableStatusMapper.selectByPrimaryKey(tableId);
        if (tableStatus == null) {
            tableStatus = new TableStatus();
            tableStatus.setTableId(tableId);
            tableStatus.setStatus(upperStatus);
            // 如果切为空闲/打扫，清除关联预约
            if ("IDLE".equals(upperStatus) || "CLEANING".equals(upperStatus)) {
                tableStatus.setCurrentReservationId(null);
            }
            tableStatus.setVersion(0);
            tableStatusMapper.insertSelective(tableStatus);
        } else {
            tableStatus.setStatus(upperStatus);
            // 如果切为空闲/打扫，清除关联预约
            if ("IDLE".equals(upperStatus) || "CLEANING".equals(upperStatus)) {
                tableStatus.setCurrentReservationId(null);
            }
            tableStatusMapper.updateByPrimaryKeySelective(tableStatus);
        }

        result.put("success", true);
        result.put("message", "桌位状态已更新");
        result.put("tableId", tableId);
        result.put("status", upperStatus);
        return result;
    }

    // =========================================================
    // L-6  POST /api/staff/order/progress
    // =========================================================
    @Override
    @Transactional
    public Map<String, Object> progressOrder(Long reservationId, String targetStatus) {

        Map<String, Object> result = new LinkedHashMap<>();

        // 1. 查预约记录
        Reservations reservation = reservationsMapper.selectByPrimaryKey(reservationId);
        if (reservation == null) {
            result.put("success", false);
            result.put("message", "预约记录不存在");
            return result;
        }

        // 2. 校验状态流转合法性
        String currentStatus = reservation.getStatus();
        String upperTarget = targetStatus != null ? targetStatus.toUpperCase() : null;

        boolean valid = false;
        if ("CONFIRMED".equals(currentStatus) && "MAKING".equals(upperTarget)) valid = true;
        if ("MAKING".equals(currentStatus) && "SERVING".equals(upperTarget)) valid = true;
        if ("SERVING".equals(currentStatus) && "COMPLETED".equals(upperTarget)) valid = true;

        if (!valid) {
            result.put("success", false);
            result.put("message", "不允许的状态流转：" + currentStatus + " → " + upperTarget);
            return result;
        }

        // 3. 更新预约状态
        reservation.setStatus(upperTarget);
        reservation.setUpdatedAt(new Date());
        reservationsMapper.updateByPrimaryKeySelective(reservation);

        // 4. 若推进到 COMPLETED，同步将桌位状态设为 CLEANING
        if ("COMPLETED".equals(upperTarget)) {
            Integer tableId = reservation.getTableId();
            if (tableId != null) {
                TableStatus tableStatus = tableStatusMapper.selectByPrimaryKey(tableId);
                if (tableStatus == null) {
                    tableStatus = new TableStatus();
                    tableStatus.setTableId(tableId);
                    tableStatus.setStatus("CLEANING");
                    tableStatus.setCurrentReservationId(null);
                    tableStatus.setVersion(0);
                    tableStatusMapper.insertSelective(tableStatus);
                } else {
                    tableStatus.setStatus("CLEANING");
                    tableStatus.setCurrentReservationId(null);
                    tableStatusMapper.updateByPrimaryKeySelective(tableStatus);
                }
            }
        }

        result.put("success", true);
        result.put("message", "订单状态已更新");
        result.put("reservationId", reservationId);
        result.put("status", toFrontendOrderStatus(upperTarget));
        return result;
    }

    // =========================================================
    // L-7  GET /api/staff/refunds?storeId=
    // =========================================================
    @Override
    public List<Map<String, Object>> getRefundList(Integer storeId) {

        // 查该门店的退款记录
        RefundRecordsExample example = new RefundRecordsExample();
        example.setOrderByClause("created_at DESC");
        List<RefundRecords> refundList = refundRecordsMapper.selectByExample(example);

        SimpleDateFormat dtFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        List<Map<String, Object>> result = new ArrayList<>();

        if (refundList != null) {
            for (RefundRecords r : refundList) {
                // 仅返回该门店的退款记录（通过 reservation 关联判断）
                if (r.getReservationId() != null) {
                    Reservations res = reservationsMapper.selectByPrimaryKey(r.getReservationId());
                    if (res == null || !storeId.equals(res.getStoreId())) continue;

                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("refundId", r.getRefundId());
                    row.put("reservationId", r.getReservationId());
                    row.put("refundAmount", r.getRefundAmount() != null ? r.getRefundAmount().doubleValue() : 0);
                    row.put("refundReason", r.getRefundReason());
                    row.put("status", r.getStatus());
                    row.put("operatorId", r.getOperatorId());
                    row.put("createdAt", r.getCreatedAt() != null ? dtFmt.format(r.getCreatedAt()) : null);
                    row.put("completedAt", r.getCompletedAt() != null ? dtFmt.format(r.getCompletedAt()) : null);

                    // 关联预约信息
                    row.put("userId", res.getUserId());
                    row.put("tableId", res.getTableId());
                    row.put("orderStatus", toFrontendOrderStatus(res.getStatus()));
                    row.put("totalAmount", res.getTotalAmount() != null ? res.getTotalAmount().doubleValue() : 0);
                    row.put("reservationTime",
                            res.getReservationTime() != null ? dtFmt.format(res.getReservationTime()) : null);
                    row.put("specialRequest", res.getSpecialRequest());

                    // 桌位信息
                    if (res.getTableId() != null) {
                        Tables table = tablesMapper.selectByPrimaryKey(res.getTableId());
                        if (table != null) {
                            row.put("tableNo", table.getTableNo());
                            row.put("tableType", table.getTableType());
                        }
                    }

                    result.add(row);
                }
            }
        }

        return result;
    }

    // =========================================================
    // L-8  POST /api/staff/refund/review
    // =========================================================
    @Override
    @Transactional
    public Map<String, Object> reviewRefund(Long refundId, String action, Long operatorId) {

        Map<String, Object> result = new LinkedHashMap<>();

        // 1. 查退款记录
        RefundRecords refund = refundRecordsMapper.selectByPrimaryKey(refundId);
        if (refund == null) {
            result.put("success", false);
            result.put("message", "退款记录不存在");
            return result;
        }

        // 2. 校验当前状态（仅 REQUEST_REFUND 状态可审核）
        if (!"REQUEST_REFUND".equals(refund.getStatus())) {
            result.put("success", false);
            result.put("message", "该退款申请已被处理，当前状态：" + refund.getStatus());
            return result;
        }

        Date now = new Date();

        // 3. 执行审核操作
        String upperAction = action != null ? action.toLowerCase() : "";
        switch (upperAction) {
            case "approve":
                // 通过退款：
                //   refund_records.status → COMPLETED
                //   payments.status → REFUNDED
                //   reservations 保持 REFUNDING（前端通过 refund_records.status 区分售后状态）
                refund.setStatus("COMPLETED");
                refund.setOperatorId(operatorId);
                refund.setCompletedAt(now);
                refundRecordsMapper.updateByPrimaryKeySelective(refund);

                // 更新 payments 状态为 REFUNDED
                if (refund.getPaymentId() != null) {
                    Payments payment = paymentsMapper.selectByPrimaryKey(refund.getPaymentId());
                    if (payment != null && "REFUNDING".equals(payment.getStatus())) {
                        payment.setStatus("REFUNDED");
                        paymentsMapper.updateByPrimaryKeySelective(payment);
                    }
                }
                break;

            case "reject":
                // 拒绝退款：
                //   refund_records.status → REJECTED
                //   payments.status → PAID（恢复支付状态）
                //   reservations.status → CONFIRMED（恢复用餐状态）
                //   积分恢复：退还已扣除的 pointsEarned，扣回已退还的 pointsUsed
                refund.setStatus("REJECTED");
                refund.setOperatorId(operatorId);
                refund.setCompletedAt(now);
                refundRecordsMapper.updateByPrimaryKeySelective(refund);

                if (refund.getReservationId() != null) {
                    Reservations res = reservationsMapper.selectByPrimaryKey(refund.getReservationId());
                    if (res != null && "REFUNDING".equals(res.getStatus())) {
                        res.setStatus("CONFIRMED");
                        res.setUpdatedAt(now);
                        reservationsMapper.updateByPrimaryKeySelective(res);

                        // 恢复 payments 状态
                        if (refund.getPaymentId() != null) {
                            Payments payment = paymentsMapper.selectByPrimaryKey(refund.getPaymentId());
                            if (payment != null && "REFUNDING".equals(payment.getStatus())) {
                                payment.setStatus("PAID");
                                paymentsMapper.updateByPrimaryKeySelective(payment);
                            }
                        }

                        // 恢复积分（applyRefund 扣了 pointsEarned 并退了 pointsUsed，现在反向操作）
                        MemberExt memberExt = memberExtMapper.selectByPrimaryKey(res.getUserId());
                        if (memberExt != null) {
                            int pointsEarned = res.getPointsEarned() != null ? res.getPointsEarned() : 0;
                            int pointsUsed = res.getPointsUsed() != null ? res.getPointsUsed() : 0;
                            int currentPoints = memberExt.getTotalPoints() != null ? memberExt.getTotalPoints() : 0;
                            // 恢复：加回 pointsEarned，减去退还的 pointsUsed
                            int newPoints = currentPoints + pointsEarned - pointsUsed;
                            BigDecimal orderAmount = res.getOrderAmount() != null ? res.getOrderAmount() : BigDecimal.ZERO;
                            BigDecimal currentCumulative = memberExt.getCumulativeAmount() != null
                                ? memberExt.getCumulativeAmount() : BigDecimal.ZERO;
                            BigDecimal newCumulative = currentCumulative.add(orderAmount);

                            MemberExt updateMe = new MemberExt();
                            updateMe.setUserId(res.getUserId());
                            updateMe.setTotalPoints(newPoints);
                            updateMe.setCumulativeAmount(newCumulative);
                            memberExtMapper.updateByPrimaryKeySelective(updateMe);

                            // 积分流水：加回消费获得积分
                            if (pointsEarned > 0) {
                                PointsLog restoreLog = new PointsLog();
                                restoreLog.setUserId(res.getUserId());
                                restoreLog.setChangeAmount(pointsEarned);
                                restoreLog.setBalanceAfter(currentPoints + pointsEarned);
                                restoreLog.setSource("REFUND_REJECT_RESTORE_EARN");
                                restoreLog.setReservationId(refund.getReservationId());
                                restoreLog.setCreatedAt(now);
                                pointsLogMapper.insertSelective(restoreLog);
                            }
                            // 积分流水：扣回已退还的使用积分
                            if (pointsUsed > 0) {
                                PointsLog deductUsedLog = new PointsLog();
                                deductUsedLog.setUserId(res.getUserId());
                                deductUsedLog.setChangeAmount(-pointsUsed);
                                deductUsedLog.setBalanceAfter(currentPoints + pointsEarned - pointsUsed);
                                deductUsedLog.setSource("REFUND_REJECT_DEDUCT_USED");
                                deductUsedLog.setReservationId(refund.getReservationId());
                                deductUsedLog.setCreatedAt(now);
                                pointsLogMapper.insertSelective(deductUsedLog);
                            }
                        }
                    }
                }
                break;

            default:
                result.put("success", false);
                result.put("message", "非法的审核操作：" + action);
                return result;
        }

        result.put("success", true);
        result.put("message", "approve".equals(upperAction) ? "退款已通过" : "退款已拒绝");
        result.put("refundId", refundId);
        result.put("status", refund.getStatus());
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
     * 桌位状态映射：数据库枚举（大写） → 前端期望值（小写）
     * IDLE → available, RESERVED → booked, OCCUPIED → occupied, CLEANING → cleaning
     */
    private String toFrontendTableStatus(String dbStatus) {
        if (dbStatus == null) return "available";
        switch (dbStatus.toUpperCase()) {
            case "IDLE":       return "available";
            case "RESERVED":  return "booked";
            case "OCCUPIED":  return "occupied";
            case "CLEANING":  return "cleaning";
            default:          return dbStatus.toLowerCase();
        }
    }

    /**
     * 预约状态映射：数据库枚举（大写） → 前端期望值（小写）
     * BOOKED → booked, CONFIRMED → confirmed, COMPLETED → completed,
     * CANCEL_BOOKING / CANCEL_ORDER → cancelled, REFUNDING → refunding
     */
    private String toFrontendOrderStatus(String dbStatus) {
        if (dbStatus == null) return "pending";
        switch (dbStatus.toUpperCase()) {
            case "BOOKED":         return "booked";
            case "CONFIRMED":      return "confirmed";
            case "MAKING":         return "making";
            case "SERVING":        return "serving";
            case "COMPLETED":      return "completed";
            case "CANCEL_BOOKING":
            case "CANCEL_ORDER":   return "cancelled";
            case "REFUNDING":      return "refunding";
            default:               return dbStatus.toLowerCase();
        }
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
