package cn.edu.bjfu.nekocafe.service.impl;

import cn.edu.bjfu.nekocafe.common.ErrorCode;
import cn.edu.bjfu.nekocafe.dto.ReservationCreateDTO;
import cn.edu.bjfu.nekocafe.dto.RescheduleDTO;
import cn.edu.bjfu.nekocafe.entity.*;
import cn.edu.bjfu.nekocafe.exception.BusinessException;
import cn.edu.bjfu.nekocafe.mapper.*;
import cn.edu.bjfu.nekocafe.service.ReservationService;
import cn.edu.bjfu.nekocafe.vo.CurrentReservationVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 预约服务实现
 * 拆分自 OrderServiceImpl — 负责纯预约、改约、重新激活等预约相关业务
 *
 * orderId 格式保持不变："ORD" + reservationId（补零到10位）
 */
@Service
public class ReservationServiceImpl implements ReservationService {

    @Autowired
    private ReservationsMapper reservationsMapper;

    @Autowired
    private StoresMapper storesMapper;

    @Autowired
    private TablesMapper tablesMapper;

    @Autowired
    private TableStatusMapper tableStatusMapper;

    @Autowired
    private RefundRecordsMapper refundRecordsMapper;

    // ==================== E-7: 纯预约（创建预约，无点单） ====================

    @Override
    @Transactional
    public Map<String, Object> createReservation(Long userId, ReservationCreateDTO dto) {
        // 1. 参数校验
        if (dto.getStoreId() == null || dto.getTableId() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "门店ID和桌位ID不能为空");
        }

        // 2. 解析预约时间
        Date reserveTime;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            reserveTime = sdf.parse(dto.getReserveDate() + " " + dto.getReserveTime());
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "预约时间格式错误");
        }

        int durationMin = dto.getDuration() != null ? dto.getDuration() * 60 : 120; // 小时转分钟

        // 2.5. 时段冲突检查：查询该桌位在目标时段是否已有活跃预约
        Calendar requestCal = Calendar.getInstance();
        requestCal.setTime(reserveTime);
        requestCal.add(Calendar.MINUTE, durationMin);
        Date requestEnd = requestCal.getTime();

        List<Reservations> tableReservations = reservationsMapper.selectByTableIdAndStatuses(
                dto.getTableId(), ACTIVE_STATUSES);
        if (tableReservations != null) {
            for (Reservations r : tableReservations) {
                if (r.getReservationTime() == null) continue;
                Date rStart = r.getReservationTime();
                Calendar rCal = Calendar.getInstance();
                rCal.setTime(rStart);
                rCal.add(Calendar.MINUTE, r.getDurationMin() != null ? r.getDurationMin() : 120);
                Date rEnd = rCal.getTime();

                // 时段重叠：已有开始 < 请求结束 AND 已有结束 > 请求开始
                if (rStart.before(requestEnd) && rEnd.after(reserveTime)) {
                    throw new BusinessException(ErrorCode.BAD_REQUEST, "该桌位在所选时段已被预约，请选择其他桌位或时段");
                }
            }
        }

        // 3. INSERT Reservations 记录（无点单，items 为空）
        Date now = new Date();
        int partySize = dto.getPersons() != null ? dto.getPersons() : 1;

        Reservations reservation = new Reservations();
        reservation.setUserId(userId);
        reservation.setStoreId(dto.getStoreId());
        reservation.setTableId(dto.getTableId());
        reservation.setReservationTime(reserveTime);
        reservation.setDurationMin(durationMin);
        reservation.setPartySize(partySize);
        reservation.setSpecialRequest(null); // 纯预约无特殊要求，可为空
        reservation.setTotalAmount(java.math.BigDecimal.ZERO);
        reservation.setOrderAmount(java.math.BigDecimal.ZERO);
        reservation.setStatus("BOOKED");
        reservation.setCreatedAt(now);

        reservationsMapper.insertSelective(reservation);
        Long reservationId = reservation.getReservationId();

        // 4. 乐观锁更新 table_status：不要求 IDLE（支持不同时段多预约），仅用 version 防并发
        TableStatus currentStatus = tableStatusMapper.selectByPrimaryKey(dto.getTableId());
        if (currentStatus == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "桌位状态记录不存在");
        }

        int affected = tableStatusMapper.reserveTableOptimistic(
                dto.getTableId(), reservationId, currentStatus.getVersion());
        if (affected == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "桌位状态已变更，请重试");
        }

        // 5. 返回 orderId + status（前端只校验 code===0，然后用自身 state 跳转）
        Map<String, Object> result = new HashMap<>();
        result.put("orderId", formatOrderId(reservationId));
        result.put("status", "BOOKED");
        return result;
    }

    // ==================== E-5: 改约 ====================

    @Override
    @Transactional
    public Map<String, Object> reschedule(Long userId, RescheduleDTO dto) {
        Long reservationId = parseOrderId(dto.getOrderId());
        if (reservationId == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "订单号格式错误");
        }

        Reservations reservation = reservationsMapper.selectByPrimaryKey(reservationId);
        if (reservation == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在");
        }
        if (userId == null || reservation.getUserId() == null
                || !reservation.getUserId().toString().equals(userId.toString())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作此订单");
        }
        if (!canRescheduleOrder(reservation)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "当前订单状态不允许改约");
        }

        // 1. UPDATE reservations: status BOOKED → CANCEL_BOOKING
        Reservations updateRes = new Reservations();
        updateRes.setReservationId(reservationId);
        updateRes.setStatus("CANCEL_BOOKING");
        updateRes.setUpdatedAt(new Date());
        reservationsMapper.updateByPrimaryKeySelective(updateRes);

        // 2. 仅在该桌无其他活跃预约时才释放 table_status
        if (reservation.getTableId() != null) {
            int otherCount = reservationsMapper.countActiveByTableIdExcluding(
                    reservation.getTableId(), ACTIVE_STATUSES, reservationId);
            if (otherCount == 0) {
                TableStatus currentStatus = tableStatusMapper.selectByPrimaryKey(reservation.getTableId());
                if (currentStatus == null) {
                    throw new BusinessException(ErrorCode.NOT_FOUND, "桌位状态记录不存在");
                }

                int affected = tableStatusMapper.releaseTableOptimistic(
                        reservation.getTableId(), reservationId, currentStatus.getVersion());
                if (affected == 0) {
                    throw new BusinessException(ErrorCode.NOT_FOUND, "桌位状态已变更，请重试");
                }
            }
        }

        // 3. 返回（前端只校验 code===0，然后 loadDetail）
        Map<String, Object> result = new HashMap<>();
        result.put("orderId", dto.getOrderId());
        result.put("status", "CANCEL_BOOKING");
        return result;
    }

    // ==================== 获取当前预约列表（供点单页面选择） ====================

    @Override
    public List<CurrentReservationVO> getCurrentReservations(Long userId) {
        // 查用户当前所有 BOOKED 状态的预约
        List<Reservations> bookedList = reservationsMapper.selectByUserIdAndStatuses(
                userId, Arrays.asList("BOOKED"));
        if (bookedList == null || bookedList.isEmpty()) {
            return new ArrayList<>();
        }

        SimpleDateFormat dateSdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeSdf = new SimpleDateFormat("HH:mm");
        List<CurrentReservationVO> result = new ArrayList<>();

        for (Reservations r : bookedList) {
            CurrentReservationVO vo = new CurrentReservationVO();
            vo.setOrderId(formatOrderId(r.getReservationId()));
            vo.setStoreId(r.getStoreId());
            vo.setTableId(r.getTableId());
            vo.setPersons(r.getPartySize());
            vo.setRemark(r.getSpecialRequest() != null ? r.getSpecialRequest() : "");

            if (r.getDurationMin() != null) {
                vo.setDuration(r.getDurationMin() / 60);
            }

            if (r.getReservationTime() != null) {
                vo.setReserveDate(dateSdf.format(r.getReservationTime()));
                vo.setReserveTime(timeSdf.format(r.getReservationTime()));
            }

            // 查门店
            if (r.getStoreId() != null) {
                Stores store = storesMapper.selectByPrimaryKey(r.getStoreId());
                if (store != null) {
                    vo.setStoreName(store.getName());
                }
            }

            // 查桌位
            if (r.getTableId() != null) {
                Tables table = tablesMapper.selectByPrimaryKey(r.getTableId());
                if (table != null) {
                    vo.setTableName(table.getTableNo());
                    vo.setTableType(table.getTableType());
                    vo.setTableCapacity(table.getCapacity());
                    vo.setCatTheme(table.getCatTheme());
                    vo.setCatName(""); // tables 表无直接关联猫名
                }
            }

            result.add(vo);
        }

        return result;
    }

    // ==================== 重新激活已取消订单（CANCEL_ORDER → BOOKED，复用同一预约） ====================

    @Override
    @Transactional
    public Map<String, Object> reactivateReservation(Long userId, String orderId) {
        Long reservationId = parseOrderId(orderId);
        if (reservationId == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "订单号格式错误");
        }

        Reservations reservation = reservationsMapper.selectByPrimaryKey(reservationId);
        if (reservation == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在");
        }
        if (userId == null || reservation.getUserId() == null
                || !reservation.getUserId().toString().equals(userId.toString())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作此订单");
        }
        // 判断是否为"已取消订单"（联合预约表+退单表判断）
        // 支持两种情况：
        //   1. reservations.status = CANCEL_ORDER（新数据）
        //   2. reservations.status = REFUNDING + refund_records 含 CANCEL（旧数据兼容）
        String dbStatus = reservation.getStatus();
        boolean isCancelOrder = "CANCEL_ORDER".equals(dbStatus);
        if (!isCancelOrder && ("REFUNDING".equals(dbStatus) || "REFUNDED".equals(dbStatus))) {
            RefundRecordsExample rrEx = new RefundRecordsExample();
            rrEx.createCriteria().andReservationIdEqualTo(reservationId);
            List<RefundRecords> rrList = refundRecordsMapper.selectByExample(rrEx);
            if (rrList != null && !rrList.isEmpty()) {
                String effectiveStatus = mapStatusToFrontend(dbStatus, rrList.get(0));
                isCancelOrder = "cancel_order".equals(effectiveStatus);
            }
        }
        if (!isCancelOrder) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "当前订单状态不允许重新激活，仅已取消订单可激活");
        }

        // 变更状态：CANCEL_ORDER → BOOKED
        Date now = new Date();
        Reservations updateRes = new Reservations();
        updateRes.setReservationId(reservationId);
        updateRes.setStatus("BOOKED");
        updateRes.setUpdatedAt(now);
        reservationsMapper.updateByPrimaryKeySelective(updateRes);

        // 查询门店和桌位名称
        SimpleDateFormat dateSdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeSdf = new SimpleDateFormat("HH:mm");

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", formatOrderId(reservationId));
        result.put("status", "BOOKED");
        result.put("storeId", reservation.getStoreId());
        result.put("tableId", reservation.getTableId());
        if (reservation.getReservationTime() != null) {
            result.put("reserveDate", dateSdf.format(reservation.getReservationTime()));
            result.put("reserveTime", timeSdf.format(reservation.getReservationTime()));
        }
        if (reservation.getDurationMin() != null) {
            result.put("duration", reservation.getDurationMin() / 60);
        }
        result.put("persons", reservation.getPartySize());

        // 查询门店名
        Stores store = storesMapper.selectByPrimaryKey(reservation.getStoreId());
        if (store != null) {
            result.put("storeName", store.getName() != null ? store.getName() : "");
        }

        // 查询桌位名/类型/容量
        Tables table = tablesMapper.selectByPrimaryKey(reservation.getTableId());
        if (table != null) {
            result.put("tableName", table.getTableNo() != null ? table.getTableNo() : "");
            result.put("tableType", table.getTableType() != null ? table.getTableType() : "");
            result.put("tableCapacity", table.getCapacity());
        }

        return result;
    }

    // ==================== 工具方法（与 OrderServiceImpl 中的原始逻辑完全一致） ====================

    private static final List<String> ACTIVE_STATUSES = Arrays.asList("BOOKED", "CONFIRMED");

    /**
     * orderId 格式："ORD" + reservationId（补零到10位）
     */
    private String formatOrderId(Long reservationId) {
        return "ORD" + String.format("%010d", reservationId);
    }

    /**
     * 从 orderId 解析 reservationId
     */
    private Long parseOrderId(String orderId) {
        if (orderId == null || !orderId.startsWith("ORD")) {
            return null;
        }
        try {
            return Long.parseLong(orderId.substring(3));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 判断订单是否可改约
     * 规则：状态为 confirmed 或 booked 时可以改约
     */
    private boolean canRescheduleOrder(Reservations reservation) {
        if (reservation == null || reservation.getStatus() == null) return false;
        String status = reservation.getStatus();
        return "CONFIRMED".equals(status) || "BOOKED".equals(status);
    }

    /**
     * 数据库订单状态 → 前端可识别状态
     */
    private String mapStatusToFrontend(String dbStatus) {
        return mapStatusToFrontend(dbStatus, null);
    }

    /**
     * 带退款记录的状态映射
     * @param dbStatus reservations.status
     * @param refund   refund_records 记录（可为 null）
     *
     * 售后相关状态（同时看预约表和退单表）：
     *   reservations.status=REFUNDING + refund_records.status=REQUEST_REFUND → after_sales_pending   （售后中）
     *   reservations.status=REFUNDING + refund_records.status=REJECTED       → after_sales_rejected  （拒绝售后）
     *   reservations.status=REFUNDING + refund_records.status=COMPLETED      → after_sales_completed （售后完成）
     */
    private String mapStatusToFrontend(String dbStatus, RefundRecords refund) {
        if (dbStatus == null) return "booked";

        // ═══ 预先判断：退单表显示这是"取消订单自动退款"还是"售后申请" ═══
        // 核心规则：联系预约表 + 退单表一起看
        //   - 退单表 status 含 CANCEL / reason 含"取消" → 取消订单（不是售后）
        //   - 退单表 status = REQUEST_REFUND / REJECTED     → 售后流程
        boolean isCancelRefund = false;
        if (refund != null) {
            String rrStatus = refund.getStatus();
            String rrReason = refund.getRefundReason();
            isCancelRefund = rrStatus != null && (
                    rrStatus.toUpperCase().contains("CANCEL") ||
                            (rrStatus.equalsIgnoreCase("COMPLETED") && rrReason != null && rrReason.contains("取消"))
            );
        }

        switch (dbStatus) {
            case "BOOKED":
                return "booked";
            case "CONFIRMED":
                return "confirmed";
            case "OCCUPIED":
                return "occupied";
            case "COMPLETED":
                return "completed";
            case "CANCEL_BOOKING":
                return "cancel_booking";   // 取消预约
            case "CANCEL_ORDER":
                return "cancel_order";     // 取消订单
            case "REFUNDING":
                // 联合退单表判断：是"取消订单退款"还是"售后审核"
                if (isCancelRefund) {
                    return "cancel_order";   // 取消订单导致的退款（含旧数据 REQUEST_CANCEL）
                }
                // 真正的售后流程
                if (refund != null) {
                    String refundStatus = refund.getStatus();
                    if ("COMPLETED".equalsIgnoreCase(refundStatus)) {
                        return "after_sales_completed";  // 售后完成
                    } else if ("REJECTED".equalsIgnoreCase(refundStatus)) {
                        return "after_sales_rejected";   // 拒绝售后
                    } else if ("REQUEST_REFUND".equalsIgnoreCase(refundStatus)) {
                        return "after_sales_pending";    // 售后中
                    }
                }
                return "after_sales_pending";        // 默认视为售后中
            case "REFUNDED":
                // 兼容旧数据：reservations.status=REFUNDED
                if (isCancelRefund) {
                    return "cancel_order";   // 取消订单退款（旧数据兼容）
                }
                if (refund != null && "COMPLETED".equalsIgnoreCase(refund.getStatus())) {
                    return "after_sales_completed";
                }
                return "after_sales_pending";
            default:
                return dbStatus.toLowerCase();
        }
    }
}
