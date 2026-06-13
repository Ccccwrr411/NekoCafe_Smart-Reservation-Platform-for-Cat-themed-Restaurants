package cn.edu.bjfu.nekocafe.service.impl;

import cn.edu.bjfu.nekocafe.common.ErrorCode;
import cn.edu.bjfu.nekocafe.dto.OrderSubmitDTO;
import cn.edu.bjfu.nekocafe.entity.*;
import cn.edu.bjfu.nekocafe.exception.BusinessException;
import cn.edu.bjfu.nekocafe.mapper.*;
import cn.edu.bjfu.nekocafe.service.OrderService;
import cn.edu.bjfu.nekocafe.vo.OrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单 & 预约服务实现
 * 负责人：C同学
 *
 * 实现要点：
 *   orderId 格式："ORD" + reservationId（补零到10位），如 ORD0000000001
 *   订单状态映射（reservations.status 字段）：
 *     pending → 待支付, confirmed → 已确认, completed → 已完成
 *     cancelled → 已取消, refunding → 退款中, refunded → 已退款
 *   durationMin 转 duration：durationMin / 60
 *   canCancel / canReschedule / canRefund 根据 status 和 reservationTime 判断
 */
@Service
public class OrderServiceImpl implements OrderService {

  @Autowired
  private ReservationsMapper reservationsMapper;

  @Autowired
  private OrderItemsMapper orderItemsMapper;

  @Autowired
  private StoresMapper storesMapper;

  @Autowired
  private TablesMapper tablesMapper;

  @Autowired
  private RefundRecordsMapper refundRecordsMapper;

  @Autowired
  private PaymentsMapper paymentsMapper;

  @Autowired
  private TableStatusMapper tableStatusMapper;

  @Autowired
  private UserCouponsMapper userCouponsMapper;

  @Autowired
  private CouponUsageMapper couponUsageMapper;

  @Autowired
  private PointsLogMapper pointsLogMapper;

  @Autowired
  private MemberExtMapper memberExtMapper;

  @Autowired
  private DishesMapper dishesMapper;

  @Autowired
  private PromotionsMapper promotionsMapper;

  @Autowired
  private UsersMapper usersMapper;

  // ==================== E-1: 订单列表 ====================

  @Override
  public List<OrderVO> listOrders(Long userId, String status, String keyword) {
    // 1. 查该用户预约（使用专用 Mapper 方法，对 status 枚举字段显式 CAST，解决 PostgreSQL 类型不匹配问题）
    List<Reservations> reservationsList;
    if (status != null && !status.isEmpty() && !"all".equals(status)) {
      List<String> dbStatuses = mapFrontendStatusToDb(status);
      if (dbStatuses != null && !dbStatuses.isEmpty()) {
        reservationsList = reservationsMapper.selectByUserIdAndStatuses(userId, dbStatuses);
      } else {
        reservationsList = reservationsMapper.selectByUserId(userId);
      }
    } else {
      reservationsList = reservationsMapper.selectByUserId(userId);
    }

    if (reservationsList == null || reservationsList.isEmpty()) {
      return new ArrayList<>();
    }

    // 收集所有 reservationId
    List<Long> reservationIds = new ArrayList<>();
    for (Reservations r : reservationsList) {
      reservationIds.add(r.getReservationId());
    }

    // 2. 批量查 OrderItems（关联表 - 用于菜品明细 + item_count）
    OrderItemsExample itemsExample = new OrderItemsExample();
    itemsExample.createCriteria().andReservationIdIn(reservationIds);
    List<OrderItems> allItems = orderItemsMapper.selectByExample(itemsExample);

    // 按 reservationId 分组
    Map<Long, List<OrderItems>> itemsMap = new HashMap<>();
    for (OrderItems item : allItems) {
      itemsMap.computeIfAbsent(item.getReservationId(), k -> new ArrayList<>()).add(item);
    }

    // 收集所有 dishId，批量查 Dishes 获取真实菜名
    Set<Integer> dishIdSet = new HashSet<>();
    for (OrderItems item : allItems) {
      if (item.getDishId() != null) {
        dishIdSet.add(item.getDishId());
      }
    }
    Map<Integer, Dishes> dishesMap = new HashMap<>();
    if (!dishIdSet.isEmpty()) {
      DishesExample dishesExample = new DishesExample();
      dishesExample.createCriteria().andDishIdIn(new ArrayList<>(dishIdSet));
      List<Dishes> dishesList = dishesMapper.selectByExample(dishesExample);
      for (Dishes d : dishesList) {
        dishesMap.put(d.getDishId(), d);
      }
    }

    // 3. 批量查门店（关联表：stores - name, address）
    Set<Integer> storeIds = new HashSet<>();
    for (Reservations r : reservationsList) {
      storeIds.add(r.getStoreId());
    }
    Map<Integer, Stores> storeMap = new HashMap<>();
    if (!storeIds.isEmpty()) {
      StoresExample storesExample = new StoresExample();
      storesExample.createCriteria().andStoreIdIn(new ArrayList<>(storeIds));
      List<Stores> storesList = storesMapper.selectByExample(storesExample);
      for (Stores s : storesList) {
        storeMap.put(s.getStoreId(), s);
      }
    }

    // 4. 批量查桌位（关联表：tables - table_no, capacity, table_type）
    Set<Integer> tableIds = new HashSet<>();
    for (Reservations r : reservationsList) {
      if (r.getTableId() != null) {
        tableIds.add(r.getTableId());
      }
    }
    Map<Integer, Tables> tableMap = new HashMap<>();
    if (!tableIds.isEmpty()) {
      TablesExample tablesExample = new TablesExample();
      tablesExample.createCriteria().andTableIdIn(new ArrayList<>(tableIds));
      List<Tables> tablesList = tablesMapper.selectByExample(tablesExample);
      for (Tables t : tablesList) {
        tableMap.put(t.getTableId(), t);
      }
    }

    // 5. 批量查支付记录（关联表：payments - amount, payment_method, status, paid_at）
    PaymentsExample payExample = new PaymentsExample();
    payExample.createCriteria().andReservationIdIn(reservationIds);
    List<Payments> allPayments = paymentsMapper.selectByExample(payExample);
    Map<Long, Payments> paymentsMap = new HashMap<>();
    for (Payments p : allPayments) {
      paymentsMap.put(p.getReservationId(), p);
    }

    // 5.5 批量查退款记录（refund_records - 用于判断退款完成）
    RefundRecordsExample refundExample = new RefundRecordsExample();
    refundExample.createCriteria().andReservationIdIn(reservationIds);
    List<RefundRecords> allRefunds = refundRecordsMapper.selectByExample(refundExample);
    Map<Long, RefundRecords> refundMap = new HashMap<>();
    for (RefundRecords ref : allRefunds) {
      refundMap.put(ref.getReservationId(), ref);
    }

    // 6. 拼装 OrderVO 列表
    List<OrderVO> result = new ArrayList<>();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat dateSdf = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat timeSdf = new SimpleDateFormat("HH:mm");

    for (Reservations r : reservationsList) {
      OrderVO vo = new OrderVO();

      // ---- 主表字段 (reservations) ----
      vo.setId(formatOrderId(r.getReservationId()));           // reservation_id → id
      vo.setStoreId(r.getStoreId());                            // store_id
      vo.setTableId(r.getTableId());                            // table_id

      if (r.getReservationTime() != null) {
        vo.setReserveDate(dateSdf.format(r.getReservationTime())); // reservation_time → reserveDate
        vo.setReserveTime(timeSdf.format(r.getReservationTime())); // reservation_time → reserveTime
      }
      if (r.getDurationMin() != null) {
        vo.setDuration(r.getDurationMin() / 60);                 // duration_min → duration (小时)
      }
      vo.setPersons(r.getPartySize());                           // party_size → persons
      // 数据库状态转换为前端可识别状态（退款完成看 refund_records.status）
      String frontendStatus = mapStatusToFrontend(r.getStatus(), refundMap.get(r.getReservationId()));
      vo.setStatus(frontendStatus);
      vo.setRemark(r.getSpecialRequest() != null ? r.getSpecialRequest() : ""); // special_request → remark

      // 金额（BigDecimal → Integer 分）
      if (r.getTotalAmount() != null) {
        vo.setTotalAmount(r.getTotalAmount().intValue());       // total_amount
      }
      if (r.getOrderAmount() != null) {
        // discountAmount = totalAmount - orderAmount
        int discount = r.getTotalAmount() != null
          ? r.getTotalAmount().intValue() - r.getOrderAmount().intValue()
          : 0;
        vo.setDiscountAmount(Math.max(0, discount));             // 优惠金额
        vo.setFinalAmount(r.getOrderAmount().intValue());        // order_amount → finalAmount
      }

      if (r.getCreatedAt() != null) {
        vo.setCreateTime(sdf.format(r.getCreatedAt()));          // created_at
      }

      // ---- 关联表: stores (name, address) ----
      Stores store = storeMap.get(r.getStoreId());
      if (store != null) {
        vo.setStoreName(store.getName());
        vo.setStoreAddress(store.getAddress());
      } else {
        vo.setStoreName("");
        vo.setStoreAddress("");
      }

      // ---- 关联表: tables (table_no, capacity, table_type) ----
      Tables table = tableMap.get(r.getTableId());
      if (table != null) {
        vo.setTableName(table.getTableNo());
        vo.setTableCapacity(table.getCapacity());
        vo.setTableType(table.getTableType());
      } else {
        vo.setTableName("");
      }

      // ---- 关联表: payments (amount, payment_method, status, paid_at) ----
      Payments payment = paymentsMap.get(r.getReservationId());
      if (payment != null) {
        if (payment.getPaidAt() != null) {
          vo.setPayTime(sdf.format(payment.getPaidAt()));
        }
        vo.setPayType(payment.getPaymentMethod() != null ? payment.getPaymentMethod() : "");
        vo.setPaymentMethod(payment.getPaymentMethod());
        vo.setPaymentStatus(payment.getStatus());
        if (payment.getAmount() != null) {
          // 如果 reservations.totalAmount 为0但 payments 有金额，使用 payments 金额
          if (vo.getTotalAmount() == null || vo.getTotalAmount() == 0) {
            vo.setTotalAmount(payment.getAmount().intValue());
          }
        }
      } else {
        vo.setPayTime("");
        vo.setPayType("");
      }

      // ---- 关联表: order_items（菜品明细 + item_count） ----
      List<OrderItems> items = itemsMap.getOrDefault(r.getReservationId(), new ArrayList<>());
      List<OrderVO.OrderItemVO> itemVOs = new ArrayList<>();
      for (OrderItems item : items) {
        OrderVO.OrderItemVO itemVO = new OrderVO.OrderItemVO();
        // 从 Dishes 表获取真实菜名
        Dishes dish = dishesMap.get(item.getDishId());
        if (dish != null) {
          itemVO.setName(dish.getName());
          itemVO.setCategory(dish.getCategory());
          itemVO.setImageUrl(dish.getImageUrl());
          itemVO.setDescription(dish.getDescription());
        } else {
          itemVO.setName("菜品" + item.getDishId());
        }
        itemVO.setQty(item.getQuantity());
        if (item.getUnitPrice() != null) {
          itemVO.setPrice(item.getUnitPrice().intValue());
        }
        itemVOs.add(itemVO);
      }
      vo.setItems(itemVOs);

      // ---- timeline（简易） ----
      List<OrderVO.TimelineVO> timeline = new ArrayList<>();
      if (r.getCreatedAt() != null) {
        OrderVO.TimelineVO t = new OrderVO.TimelineVO();
        t.setTime(sdf.format(r.getCreatedAt()));
        t.setTitle("订单创建");
        t.setDesc("订单已创建");
        timeline.add(t);
      }
      // 已支付时添加支付时间线
      if (payment != null && payment.getPaidAt() != null) {
        OrderVO.TimelineVO t = new OrderVO.TimelineVO();
        t.setTime(sdf.format(payment.getPaidAt()));
        t.setTitle("支付完成");
        t.setDesc("已通过" + (payment.getPaymentMethod() != null ? payment.getPaymentMethod() : "在线") + "支付");
        timeline.add(t);
      }
      // 历史取消/重新点单：通过 refund_records 追踪（即使 reactivate 后状态已恢复）
      RefundRecords historyRefund = refundMap.get(r.getReservationId());
      if (historyRefund != null) {
        String rStatus = r.getStatus();
        if (!"CANCEL_BOOKING".equals(rStatus) && !"CANCEL_ORDER".equals(rStatus)) {
          // 曾被取消但当前状态已恢复 → 显示历史取消 + 重新点单
          if (historyRefund.getCreatedAt() != null) {
            OrderVO.TimelineVO tc2 = new OrderVO.TimelineVO();
            tc2.setTime(sdf.format(historyRefund.getCreatedAt()));
            tc2.setTitle("预约取消");
            tc2.setDesc("订单已取消并退款");
            timeline.add(tc2);
          }
          if (r.getUpdatedAt() != null && historyRefund.getCreatedAt() != null
              && r.getUpdatedAt().after(historyRefund.getCreatedAt())) {
            OrderVO.TimelineVO tr2 = new OrderVO.TimelineVO();
            tr2.setTime(sdf.format(r.getUpdatedAt()));
            tr2.setTitle("重新点单");
            tr2.setDesc("用户取消后再次下单");
            timeline.add(tr2);
          }
        } else {
          // 当前仍处于取消状态
          Date cancelTime2 = r.getUpdatedAt();
          if (cancelTime2 != null) {
            OrderVO.TimelineVO tc2 = new OrderVO.TimelineVO();
            tc2.setTime(sdf.format(cancelTime2));
            tc2.setTitle("预约取消");
            tc2.setDesc("CANCEL_BOOKING".equals(rStatus) ? "用户取消了预约" : "订单已取消并退款");
            timeline.add(tc2);
          }
        }
      } else {
        // 无退款记录时的取消状态（BOOKED 直接取消无支付的情况）
        String rStatus = r.getStatus();
        if ("CANCEL_BOOKING".equals(rStatus) || "CANCEL_ORDER".equals(rStatus)) {
          Date cancelTime2 = r.getUpdatedAt();
          if (cancelTime2 != null) {
            OrderVO.TimelineVO tc2 = new OrderVO.TimelineVO();
            tc2.setTime(sdf.format(cancelTime2));
            tc2.setTitle("预约取消");
            tc2.setDesc("CANCEL_BOOKING".equals(rStatus) ? "用户取消了预约" : "订单已取消并退款");
            timeline.add(tc2);
          }
        }
      }
      vo.setTimeline(timeline);

      // ---- 操作权限 ----
      vo.setCanCancel(canCancelOrder(r));
      vo.setCanReschedule(canRescheduleOrder(r));
      vo.setCanRefund(canRefundOrder(r));
      vo.setHasReview(false); // 评价由 ReviewService 处理

      result.add(vo);
    }

    // 7. 搜索过滤（关键词匹配门店名/桌位名/订单号）
    if (keyword != null && !keyword.trim().isEmpty()) {
      String kw = keyword.trim().toLowerCase();
      result = result.stream()
          .filter(vo ->
              (vo.getStoreName() != null && vo.getStoreName().toLowerCase().contains(kw)) ||
              (vo.getTableName() != null && vo.getTableName().toLowerCase().contains(kw)) ||
              (vo.getId() != null && vo.getId().toLowerCase().contains(kw))
          )
          .collect(Collectors.toList());
    }

    // 8. 二次状态过滤（解决 SQL 无法联合退单表判断的问题）
    //    SQL 按 reservations.status 筛选后，再按实际前端状态（mapStatusToFrontend）二次过滤
    //    例：reservations=REFUNDING + refund_records=REQUEST_CANCEL → 前端=cancel_order
    //        用户选"售后"tab 时应排除此类订单（归入"已取消"）
    if (status != null && !status.isEmpty() && !"all".equals(status)) {
      final List<String> allowedFrontendStatuses = getAllowedFrontendStatuses(status);
      result = result.stream()
          .filter(vo -> allowedFrontendStatuses.contains(vo.getStatus()))
          .collect(Collectors.toList());
    }

    return result;
  }

  // ==================== E-2: 提交订单 ====================

  @Override
  @Transactional
  public Map<String, Object> submitOrder(Long userId, OrderSubmitDTO dto) {
    // 1. 参数校验
    if (dto.getStoreId() == null) {
      throw new BusinessException(ErrorCode.BAD_REQUEST, "门店ID不能为空");
    }

    Date now = new Date();
    int totalAmount = dto.getTotalAmount() != null ? dto.getTotalAmount() : 0;
    int finalAmount = dto.getFinalAmount() != null ? dto.getFinalAmount() : totalAmount;
    int discount = totalAmount - finalAmount;

    // 2. 查找预约记录
    //    优先用 orderId（格式 "ORD" + reservationId补零10位）直接定位预约
    //    若无 orderId，则回退到 userId + tableId 匹配
    Reservations reservation = null;
    if (dto.getOrderId() != null && !dto.getOrderId().isEmpty()) {
      Long rid = parseOrderId(dto.getOrderId());
      if (rid != null) {
        reservation = reservationsMapper.selectByPrimaryKey(rid);
      }
    }
    if (reservation == null && dto.getTableId() != null) {
      List<Reservations> bookedList = reservationsMapper.selectByUserIdAndTableIdAndStatus(
          userId, dto.getTableId(), "BOOKED");
      if (bookedList != null && !bookedList.isEmpty()) {
        reservation = bookedList.get(0);
      }
    }
    if (reservation == null) {
      throw new BusinessException(ErrorCode.NOT_FOUND, "未找到有效的预约记录");
    }
    // 校验该预约属于当前用户
    if (!userId.equals(reservation.getUserId())) {
      throw new BusinessException(ErrorCode.NOT_FOUND, "未找到有效的预约记录");
    }
    Long reservationId = reservation.getReservationId();

    // 3. 计算积分：points_used 暂不使用，points_earned = finalAmount（每元1分）
    int pointsUsed = 0;
    int pointsEarned = finalAmount;

    // ==================== UPDATE 操作 ====================

    // 3a. UPDATE reservations: status BOOKED → CONFIRMED，同时保存备注
    Reservations updateRes = new Reservations();
    updateRes.setReservationId(reservationId);
    updateRes.setOrderAmount(new BigDecimal(finalAmount));
    updateRes.setTotalAmount(new BigDecimal(totalAmount));
    updateRes.setPointsUsed(0);
    updateRes.setPointsEarned(pointsEarned);
    updateRes.setStatus("CONFIRMED");
    updateRes.setUpdatedAt(now);
    if (dto.getRemark() != null && !dto.getRemark().isEmpty()) {
      updateRes.setSpecialRequest(dto.getRemark());
    }
    reservationsMapper.updateByPrimaryKeySelective(updateRes);

    // 3a2. UPDATE table_status: RESERVED → OCCUPIED（下单确认后占用桌位）
    if (reservation.getTableId() != null) {
      TableStatus currentTs = tableStatusMapper.selectByPrimaryKey(reservation.getTableId());
      if (currentTs != null && "RESERVED".equals(currentTs.getStatus())) {
        int occupied = tableStatusMapper.occupyTableOptimistic(
            reservation.getTableId(), currentTs.getVersion());
        if (occupied == 0) {
          throw new BusinessException(ErrorCode.NOT_FOUND, "桌位状态已变更，请刷新重试");
        }
      }
    }

    // 3b. UPDATE user_coupons: UNUSED → USED（每个使用中的优惠券）
    if (dto.getCouponIds() != null && !dto.getCouponIds().isEmpty()) {
      for (String couponIdStr : dto.getCouponIds()) {
        Long couponId = Long.parseLong(couponIdStr);
        UserCouponsExample ucExample = new UserCouponsExample();
        ucExample.createCriteria()
          .andCouponIdEqualTo(couponId)
          .andStatusEqualTo("UNUSED");
        UserCoupons updateUc = new UserCoupons();
        updateUc.setStatus("USED");
        updateUc.setUsedAt(now);
        updateUc.setUsedReservationId(reservationId);
        userCouponsMapper.updateByExampleSelective(updateUc, ucExample);
      }
    }

    // 3c. UPDATE member_ext: 更新累计金额、积分、最后访问时间
    MemberExt memberExt = memberExtMapper.selectByPrimaryKey(userId);
    if (memberExt == null) {
      // 不存在则创建
      memberExt = new MemberExt();
      memberExt.setUserId(userId);
      memberExt.setLevel(1);
      memberExt.setCumulativeAmount(new BigDecimal(finalAmount));
      memberExt.setTotalPoints(finalAmount * 2);
      memberExt.setLastVisitTime(now);
      memberExt.setCreatedAt(now);
      memberExtMapper.insertSelective(memberExt);
    } else {
      BigDecimal oldCumulative = memberExt.getCumulativeAmount() != null
        ? memberExt.getCumulativeAmount() : BigDecimal.ZERO;
      BigDecimal newCumulative = oldCumulative.add(new BigDecimal(finalAmount));
      int newTotalPoints = newCumulative.intValue() * 2;

      MemberExt updateMe = new MemberExt();
      updateMe.setUserId(userId);
      updateMe.setCumulativeAmount(newCumulative);
      updateMe.setTotalPoints(newTotalPoints);
      updateMe.setLastVisitTime(now);
      memberExtMapper.updateByPrimaryKeySelective(updateMe);
      memberExt.setTotalPoints(newTotalPoints); // 供后续 points_log 使用
    }

    // ==================== INSERT 操作 ====================

    // 4a. INSERT order_items（每个菜品一行）
    if (dto.getItems() != null && !dto.getItems().isEmpty()) {
      for (OrderSubmitDTO.OrderItemDTO itemDTO : dto.getItems()) {
        OrderItems item = new OrderItems();
        item.setReservationId(reservationId);
        item.setDishId(itemDTO.getMenuId());
        item.setQuantity(itemDTO.getQty());
        item.setUnitPrice(new BigDecimal(itemDTO.getPrice()));
        item.setSubtotal(new BigDecimal(itemDTO.getPrice() * itemDTO.getQty()));
        orderItemsMapper.insertSelective(item);
      }
    }

    // 4b. INSERT coupon_usage（每条优惠券一条记录）
    if (dto.getCouponIds() != null && !dto.getCouponIds().isEmpty()) {
      // 简单均摊折扣（如果有多张券）
      int perCouponDiscount = dto.getCouponIds().size() > 0
        ? discount / dto.getCouponIds().size() : 0;
      for (String couponIdStr : dto.getCouponIds()) {
        CouponUsage usage = new CouponUsage();
        usage.setCouponId(Long.parseLong(couponIdStr));
        usage.setReservationId(reservationId);
        usage.setDiscountAmount(new BigDecimal(perCouponDiscount));
        usage.setUsedAt(now);
        couponUsageMapper.insertSelective(usage);
      }
    }

    // 4c. INSERT points_log（仅 ORDER_EARN 正数，points_used 暂不使用）
    int balanceAfter = memberExt.getTotalPoints() != null ? memberExt.getTotalPoints() : 0;

    // points_used 暂不使用，不记录 ORDER_USE 日志
    // if (pointsUsed > 0) { ... }

    PointsLog earnLog = new PointsLog();
    earnLog.setUserId(userId);
    earnLog.setChangeAmount(pointsEarned);
    earnLog.setBalanceAfter(balanceAfter + pointsEarned);
    earnLog.setSource("ORDER_EARN");
    earnLog.setReservationId(reservationId);
    earnLog.setCreatedAt(now);
    pointsLogMapper.insertSelective(earnLog);

    // 4d. INSERT payments
    String transactionId = "MOCK_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    Payments payment = new Payments();
    payment.setReservationId(reservationId);
    payment.setPaymentMethod("wechat");
    payment.setAmount(new BigDecimal(finalAmount));
    payment.setTransactionId(transactionId);
    payment.setStatus("PAID");
    payment.setPaidAt(now);
    payment.setCreatedAt(now);
    paymentsMapper.insertSelective(payment);

    // 5. 返回（前端检查 code===0, payInfo, finalAmount）
    //    开发阶段：返回 mock 支付参数，前端识别后走模拟支付弹窗
    //    对接真实微信支付后：需调用统一下单API获取 timeStamp/nonceStr/package/paySign
    Map<String, Object> result = new HashMap<>();
    result.put("orderId", formatOrderId(reservationId));
    result.put("totalAmount", totalAmount);
    result.put("finalAmount", finalAmount);
    Map<String, String> payInfo = new HashMap<>();
    payInfo.put("transactionId", transactionId);
    payInfo.put("method", "wechat");
    payInfo.put("status", "PAID");
    payInfo.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
    payInfo.put("nonceStr", UUID.randomUUID().toString().replace("-", "").substring(0, 16));
    payInfo.put("package", "prepay_id=mock_prepay_id_" + transactionId);
    payInfo.put("signType", "RSA");
    payInfo.put("paySign", "mock_pay_sign_20260603");
    result.put("payInfo", payInfo);

    return result;
  }

  // ==================== E-3: 订单详情 ====================

  @Override
  public OrderVO getOrderDetail(String orderId) {
    Long reservationId = parseOrderId(orderId);
    if (reservationId == null) {
      throw new BusinessException(ErrorCode.BAD_REQUEST, "订单号格式错误");
    }

    Reservations reservation = reservationsMapper.selectByPrimaryKey(reservationId);
    if (reservation == null) {
      throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在");
    }

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat dateSdf = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat timeSdf = new SimpleDateFormat("HH:mm");

    // ==================== 查主表 reservations（15字段） ====================

    OrderVO vo = new OrderVO();
    vo.setId(formatOrderId(reservationId));
    vo.setStoreId(reservation.getStoreId());
    vo.setTableId(reservation.getTableId());
    // 状态暂不设置，等 refund_records 查完后再设置（退款完成需要看 refund_records.status）
    vo.setPersons(reservation.getPartySize());
    vo.setRemark(reservation.getSpecialRequest() != null ? reservation.getSpecialRequest() : "");
    if (reservation.getReservationTime() != null) {
      vo.setReserveDate(dateSdf.format(reservation.getReservationTime()));
      vo.setReserveTime(timeSdf.format(reservation.getReservationTime()));
    }
    if (reservation.getDurationMin() != null) {
      vo.setDuration(reservation.getDurationMin() / 60);
    }
    if (reservation.getTotalAmount() != null) {
      vo.setTotalAmount(reservation.getTotalAmount().intValue());
    }
    if (reservation.getOrderAmount() != null) {
      int discount = reservation.getTotalAmount() != null
        ? reservation.getTotalAmount().intValue() - reservation.getOrderAmount().intValue()
        : 0;
      vo.setDiscountAmount(Math.max(0, discount));
      vo.setFinalAmount(reservation.getOrderAmount().intValue());
    }
    if (reservation.getCreatedAt() != null) {
      vo.setCreateTime(sdf.format(reservation.getCreatedAt()));
    }

    // ==================== 查 stores（4字段） ====================

    Stores store = storesMapper.selectByPrimaryKey(reservation.getStoreId());
    if (store != null) {
      vo.setStoreName(store.getName() != null ? store.getName() : "");
      vo.setStoreAddress(store.getAddress() != null ? store.getAddress() : "");
      vo.setStorePhone(store.getContactPhone() != null ? store.getContactPhone() : "");
    }

    // ==================== 查 tables（5字段） ====================

    Tables table = tablesMapper.selectByPrimaryKey(reservation.getTableId());
    if (table != null) {
      vo.setTableName(table.getTableNo() != null ? table.getTableNo() : "");
      vo.setTableCapacity(table.getCapacity());
      vo.setTableType(table.getTableType() != null ? table.getTableType() : "");
      vo.setCatTheme(table.getCatTheme() != null ? table.getCatTheme() : "");
    }

    // ==================== 查 table_status（1字段） ====================

    TableStatus tableStatus = tableStatusMapper.selectByPrimaryKey(reservation.getTableId());
    if (tableStatus != null) {
      vo.setTableStatus(tableStatus.getStatus());
    }

    // ==================== 查 order_items + dishes ====================

    OrderItemsExample itemsExample = new OrderItemsExample();
    itemsExample.createCriteria().andReservationIdEqualTo(reservationId);
    List<OrderItems> items = orderItemsMapper.selectByExample(itemsExample);

    // 批量查菜品名
    Map<Integer, Dishes> dishesMap = new HashMap<>();
    if (items != null && !items.isEmpty()) {
      List<Integer> dishIds = new ArrayList<>();
      for (OrderItems item : items) {
        dishIds.add(item.getDishId());
      }
      if (!dishIds.isEmpty()) {
        DishesExample dishesExample = new DishesExample();
        dishesExample.createCriteria().andDishIdIn(dishIds);
        List<Dishes> dishesList = dishesMapper.selectByExample(dishesExample);
        for (Dishes d : dishesList) {
          dishesMap.put(d.getDishId(), d);
        }
      }
    }

    List<OrderVO.OrderItemVO> itemVOs = new ArrayList<>();
    if (items != null) {
      for (OrderItems item : items) {
        OrderVO.OrderItemVO itemVO = new OrderVO.OrderItemVO();
        Dishes dish = dishesMap.get(item.getDishId());
        if (dish != null) {
          itemVO.setName(dish.getName());
          itemVO.setCategory(dish.getCategory());
          itemVO.setImageUrl(dish.getImageUrl());
          itemVO.setDescription(dish.getDescription());
        } else {
          itemVO.setName("菜品" + item.getDishId());
        }
        itemVO.setQty(item.getQuantity());
        if (item.getUnitPrice() != null) {
          itemVO.setPrice(item.getUnitPrice().intValue());
        }
        itemVOs.add(itemVO);
      }
    }
    vo.setItems(itemVOs);

    // ==================== 查 payments（6字段） ====================

    PaymentsExample payExample = new PaymentsExample();
    payExample.createCriteria().andReservationIdEqualTo(reservationId);
    List<Payments> paymentsList = paymentsMapper.selectByExample(payExample);
    Payments payment = (paymentsList != null && !paymentsList.isEmpty()) ? paymentsList.get(0) : null;
    if (payment != null) {
      if (payment.getPaidAt() != null) {
        vo.setPayTime(sdf.format(payment.getPaidAt()));
      }
      vo.setPayType(payment.getPaymentMethod() != null ? payment.getPaymentMethod() : "");
      vo.setPaymentMethod(payment.getPaymentMethod() != null ? payment.getPaymentMethod() : "");
      vo.setTransactionId(payment.getTransactionId() != null ? payment.getTransactionId() : "");
      vo.setPaymentStatus(payment.getStatus() != null ? payment.getStatus() : "");
    }

    // ==================== 查 refund_records（5字段） ====================

    RefundRecordsExample refundExample = new RefundRecordsExample();
    refundExample.createCriteria().andReservationIdEqualTo(reservationId);
    refundExample.setOrderByClause("created_at DESC");
    List<RefundRecords> refundList = refundRecordsMapper.selectByExample(refundExample);
    if (refundList != null && !refundList.isEmpty()) {
      RefundRecords refund = refundList.get(0);
      vo.setRefundReason(refund.getRefundReason());
      vo.setRefundStatus(refund.getStatus());
      if (refund.getCreatedAt() != null) {
        vo.setRefundCreatedAt(sdf.format(refund.getCreatedAt()));
      }
      if (refund.getCompletedAt() != null) {
        vo.setRefundCompletedAt(sdf.format(refund.getCompletedAt()));
      }
      // 状态映射（需要 refund_records 判断退款是否真正完成）
      vo.setStatus(mapStatusToFrontend(reservation.getStatus(), refund));
    } else {
      vo.setStatus(mapStatusToFrontend(reservation.getStatus()));
    }

    // ==================== 查 coupon_usage + user_coupons + promotions ====================

    CouponUsageExample cuExample = new CouponUsageExample();
    cuExample.createCriteria().andReservationIdEqualTo(reservationId);
    List<CouponUsage> couponUsageList = couponUsageMapper.selectByExample(cuExample);

    List<OrderVO.CouponVO> couponVOs = new ArrayList<>();
    int totalCouponDiscount = 0;
    if (couponUsageList != null && !couponUsageList.isEmpty()) {
      for (CouponUsage usage : couponUsageList) {
        OrderVO.CouponVO couponVO = new OrderVO.CouponVO();
        couponVO.setCouponId(usage.getCouponId());
        if (usage.getDiscountAmount() != null) {
          couponVO.setDiscountAmount(usage.getDiscountAmount().intValue());
          totalCouponDiscount += usage.getDiscountAmount().intValue();
        }

        // 查 user_coupons 获取状态
        UserCoupons uc = userCouponsMapper.selectByPrimaryKey(usage.getCouponId());
        if (uc != null) {
          couponVO.setStatus(uc.getStatus());
          // 查 promotions 获取名称和类型
          if (uc.getPromoId() != null) {
            Promotions promo = promotionsMapper.selectByPrimaryKey(uc.getPromoId());
            if (promo != null) {
              couponVO.setName(promo.getName());
              couponVO.setType(promo.getType());
            }
          }
        }
        couponVOs.add(couponVO);
      }
    }
    vo.setCoupons(couponVOs);
    vo.setCouponDiscount(totalCouponDiscount);

    // ==================== 查 member_ext（2字段） ====================

    MemberExt memberExt = memberExtMapper.selectByPrimaryKey(reservation.getUserId());
    if (memberExt != null) {
      vo.setMemberLevel(memberExt.getLevel());
      vo.setMemberPoints(memberExt.getTotalPoints());
    }

    // ==================== 查 users（3字段） ====================

    Users user = usersMapper.selectByPrimaryKey(reservation.getUserId());
    if (user != null) {
      vo.setCustomerName(user.getNickname() != null ? user.getNickname() : "");
      vo.setCustomerPhone(user.getPhone() != null ? user.getPhone() : "");
      vo.setCustomerAvatar(user.getAvatarUrl() != null ? user.getAvatarUrl() : "");
    }

    // ==================== 时间线 ====================

    List<OrderVO.TimelineVO> timeline = new ArrayList<>();
    if (reservation.getCreatedAt() != null) {
      OrderVO.TimelineVO t1 = new OrderVO.TimelineVO();
      t1.setTime(sdf.format(reservation.getCreatedAt()));
      t1.setTitle("订单创建");
      t1.setDesc("订单已创建");
      timeline.add(t1);
    }
    if (payment != null && payment.getPaidAt() != null) {
      OrderVO.TimelineVO t2 = new OrderVO.TimelineVO();
      t2.setTime(sdf.format(payment.getPaidAt()));
      t2.setTitle("支付完成");
      t2.setDesc("已支付 ¥" + (payment.getAmount() != null ? payment.getAmount().intValue() : 0));
      timeline.add(t2);
    }
    if (refundList != null && !refundList.isEmpty()) {
      RefundRecords refund = refundList.get(0);
      if (refund.getCreatedAt() != null) {
        OrderVO.TimelineVO t3 = new OrderVO.TimelineVO();
        t3.setTime(sdf.format(refund.getCreatedAt()));
        t3.setTitle("退款申请");
        t3.setDesc(refund.getRefundReason() != null ? refund.getRefundReason() : "退款处理中");
        timeline.add(t3);
      }
      // 历史取消记录：存在 refund_records 说明曾被取消（即使 reactivate 后状态已恢复）
      // 仅在当前状态非 CANCEL 时才补充显示（CANCEL 状态已有独立的取消条目）
      String currentStatus = reservation.getStatus();
      if (!"CANCEL_BOOKING".equals(currentStatus) && !"CANCEL_ORDER".equals(currentStatus)) {
        OrderVO.TimelineVO tc = new OrderVO.TimelineVO();
        tc.setTime(sdf.format(refund.getCreatedAt()));
        tc.setTitle("预约取消");
        tc.setDesc("订单已取消并退款");
        timeline.add(tc);
        // 重新点单（reactivate）：有退款记录但状态已恢复为正常状态，说明用户重新激活了预约
        Date reactivateTime = reservation.getUpdatedAt();
        if (reactivateTime != null && reactivateTime.after(refund.getCreatedAt())) {
          OrderVO.TimelineVO tr = new OrderVO.TimelineVO();
          tr.setTime(sdf.format(reactivateTime));
          tr.setTitle("重新点单");
          tr.setDesc("用户取消后再次下单");
          timeline.add(tr);
        }
      }
    }
    // 当前仍处于取消状态时添加时间线
    String status = reservation.getStatus();
    if ("CANCEL_BOOKING".equals(status) || "CANCEL_ORDER".equals(status)) {
      Date cancelTime = reservation.getUpdatedAt();
      if (cancelTime != null) {
        OrderVO.TimelineVO tc = new OrderVO.TimelineVO();
        tc.setTime(sdf.format(cancelTime));
        tc.setTitle("预约取消");
        tc.setDesc("CANCEL_BOOKING".equals(status) ? "用户取消了预约" : "订单已取消并退款");
        timeline.add(tc);
      }
    }
    vo.setTimeline(timeline);

    // ==================== 操作权限 ====================

    vo.setCanCancel(canCancelOrder(reservation));
    vo.setCanReschedule(false); // 不允许改约
    vo.setCanRefund(canRefundOrder(reservation));
    vo.setHasReview(false);

    // ==================== 前端展示字段 ====================

    vo.setStatusIcon(mapStatusIcon(vo.getStatus()));
    vo.setStatusDesc(mapStatusDesc(vo.getStatus()));

    return vo;
  }

  // ==================== E-4: 取消订单 ====================

  @Override
  @Transactional
  public Map<String, Object> cancelOrder(Long userId, String orderId) {
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
    if (!canCancelOrder(reservation)) {
      String status = reservation.getStatus();
      if ("BOOKED".equals(status) || "CONFIRMED".equals(status)) {
        throw new BusinessException(ErrorCode.BAD_REQUEST, "下单已超过5分钟，无法取消");
      }
      throw new BusinessException(ErrorCode.BAD_REQUEST, "当前订单状态不允许取消");
    }

    Date now = new Date();
    String currentStatus = reservation.getStatus();

    // ==================== 简单取消（BOOKED 无支付，更新状态 + 释放桌位） ====================
    if ("BOOKED".equals(currentStatus)) {
      // 1. UPDATE reservations: BOOKED → CANCEL_BOOKING
      Reservations updateRes = new Reservations();
      updateRes.setReservationId(reservationId);
      updateRes.setStatus("CANCEL_BOOKING");
      updateRes.setUpdatedAt(now);
      reservationsMapper.updateByPrimaryKeySelective(updateRes);

      // 2. 仅在该桌无其他活跃预约时才释放 table_status
      if (reservation.getTableId() != null) {
        int otherCount = reservationsMapper.countActiveByTableIdExcluding(
            reservation.getTableId(), ACTIVE_STATUSES, reservationId);
        if (otherCount == 0) {
          TableStatus currentStatus2 = tableStatusMapper.selectByPrimaryKey(reservation.getTableId());
          if (currentStatus2 != null) {
            int affected = tableStatusMapper.releaseTableOptimistic(
              reservation.getTableId(), reservationId, currentStatus2.getVersion());
            if (affected == 0) {
              throw new BusinessException(ErrorCode.NOT_FOUND, "桌位状态已变更，请重试");
            }
          }
        }
      }

      Map<String, Object> result = new HashMap<>();
      result.put("status", "CANCEL_BOOKING");
      result.put("refundAmount", 0);
      return result;
    }

    // ==================== 完整取消（CONFIRMED，含直接退款） ====================

    // 查询关联支付记录
    PaymentsExample payExample = new PaymentsExample();
    payExample.createCriteria().andReservationIdEqualTo(reservationId);
    List<Payments> payList = paymentsMapper.selectByExample(payExample);
    Payments payment = (payList != null && !payList.isEmpty()) ? payList.get(0) : null;
    boolean hasPaid = payment != null && "PAID".equals(payment.getStatus());

    // 查询是否有使用优惠券
    CouponUsageExample cuExample = new CouponUsageExample();
    cuExample.createCriteria().andReservationIdEqualTo(reservationId);
    List<CouponUsage> couponUsageList = couponUsageMapper.selectByExample(cuExample);
    boolean hasCoupon = couponUsageList != null && !couponUsageList.isEmpty();

    // 查询会员扩展信息
    MemberExt memberExt = memberExtMapper.selectByPrimaryKey(userId);
    int pointsUsed = reservation.getPointsUsed() != null ? reservation.getPointsUsed() : 0;
    int pointsEarned = reservation.getPointsEarned() != null ? reservation.getPointsEarned() : 0;

    // 退款金额 = order_amount
    BigDecimal refundAmount = reservation.getOrderAmount() != null
      ? reservation.getOrderAmount()
      : BigDecimal.ZERO;

    // ==================== UPDATE 操作 ====================

    // 1. UPDATE reservations: CONFIRMED → CANCEL_ORDER
    Reservations updateRes = new Reservations();
    updateRes.setReservationId(reservationId);
    updateRes.setStatus("CANCEL_ORDER");
    updateRes.setOrderAmount(BigDecimal.ZERO);
    updateRes.setTotalAmount(BigDecimal.ZERO);
    updateRes.setPointsUsed(0);
    updateRes.setPointsEarned(0);
    updateRes.setUpdatedAt(now);
    reservationsMapper.updateByPrimaryKeySelective(updateRes);

    // 2. UPDATE payments: PAID → REFUNDED（取消订单直接退款，不走售后审核）
    if (hasPaid) {
      Payments updatePay = new Payments();
      updatePay.setPaymentId(payment.getPaymentId());
      updatePay.setStatus("REFUNDED");
      paymentsMapper.updateByPrimaryKeySelective(updatePay);
    }

    // 3. UPDATE user_coupons: USED → ACTIVE（如使用优惠券）
    if (hasCoupon) {
      for (CouponUsage usage : couponUsageList) {
        UserCouponsExample ucExample = new UserCouponsExample();
        ucExample.createCriteria()
          .andCouponIdEqualTo(usage.getCouponId())
          .andStatusEqualTo("USED")
          .andUsedReservationIdEqualTo(reservationId);
        UserCoupons updateUc = new UserCoupons();
        updateUc.setStatus("UNUSED");
        updateUc.setUsedAt(null);
        updateUc.setUsedReservationId(null);
        userCouponsMapper.updateByExampleSelective(updateUc, ucExample);
      }
    }

    // 4. UPDATE member_ext: 扣除消费获得积分 + 退还已用积分 + 回滚累计消费
    if (memberExt != null) {
      int currentPoints = memberExt.getTotalPoints() != null ? memberExt.getTotalPoints() : 0;
      // 扣除消费获得积分，退还使用的积分（如有）
      int newPoints = currentPoints - pointsEarned + pointsUsed;
      BigDecimal currentCumulative = memberExt.getCumulativeAmount() != null
        ? memberExt.getCumulativeAmount() : BigDecimal.ZERO;
      BigDecimal newCumulative = currentCumulative.subtract(refundAmount);

      MemberExt updateMe = new MemberExt();
      updateMe.setUserId(userId);
      updateMe.setTotalPoints(newPoints);
      updateMe.setCumulativeAmount(newCumulative);
      memberExtMapper.updateByPrimaryKeySelective(updateMe);
    }

    // ==================== INSERT 操作 ====================

    // 5. INSERT refund_records: 取消订单直接退款完成（不走售后审核）
    if (hasPaid) {
      RefundRecords refund = new RefundRecords();
      refund.setPaymentId(payment.getPaymentId());
      refund.setReservationId(reservationId);
      refund.setRefundAmount(refundAmount);
      refund.setRefundReason("用户取消订单");
      refund.setStatus("COMPLETED");
      refund.setCreatedAt(now);
      refund.setCompletedAt(now);
      refundRecordsMapper.insertSelective(refund);
    }

    // 6a. INSERT points_log: 扣除消费获得的积分（ORDER_CANCEL_DEDUCT_EARN）
    if (pointsEarned > 0 && memberExt != null) {
      int currentPoints = memberExt.getTotalPoints() != null ? memberExt.getTotalPoints() : 0;
      PointsLog deductLog = new PointsLog();
      deductLog.setUserId(userId);
      deductLog.setChangeAmount(-pointsEarned);
      deductLog.setBalanceAfter(currentPoints - pointsEarned);
      deductLog.setSource("ORDER_CANCEL_DEDUCT_EARN");
      deductLog.setReservationId(reservationId);
      deductLog.setCreatedAt(now);
      pointsLogMapper.insertSelective(deductLog);
    }

    // 6b. INSERT points_log: 退还使用的积分（ORDER_CANCEL_RETURN_USED）
    if (pointsUsed > 0 && memberExt != null) {
      int currentPoints = memberExt.getTotalPoints() != null ? memberExt.getTotalPoints() : 0;
      int afterDeduct = currentPoints - pointsEarned;
      PointsLog returnLog = new PointsLog();
      returnLog.setUserId(userId);
      returnLog.setChangeAmount(pointsUsed);
      returnLog.setBalanceAfter(afterDeduct + pointsUsed);
      returnLog.setSource("ORDER_CANCEL_RETURN_USED");
      returnLog.setReservationId(reservationId);
      returnLog.setCreatedAt(now);
      pointsLogMapper.insertSelective(returnLog);
    }

    // ==================== 删除前查询消费明细 ====================

    // 7. 查询 order_items（删除前返回给前端显示，含菜品名称和图片）
    OrderItemsExample oiExample = new OrderItemsExample();
    oiExample.createCriteria().andReservationIdEqualTo(reservationId);
    List<OrderItems> orderItemsList = orderItemsMapper.selectByExample(oiExample);
    List<Map<String, Object>> items = new java.util.ArrayList<>();
    if (orderItemsList != null) {
      // 收集所有 dishId，批量查询菜品信息
      List<Integer> dishIds = orderItemsList.stream()
        .map(OrderItems::getDishId)
        .filter(id -> id != null)
        .distinct()
        .collect(java.util.stream.Collectors.toList());
      Map<Integer, Dishes> dishMap = new HashMap<>();
      if (!dishIds.isEmpty()) {
        DishesExample dishesExample = new DishesExample();
        dishesExample.createCriteria().andDishIdIn(dishIds);
        List<Dishes> dishesList = dishesMapper.selectByExample(dishesExample);
        if (dishesList != null) {
          for (Dishes d : dishesList) {
            dishMap.put(d.getDishId(), d);
          }
        }
      }
      for (OrderItems oi : orderItemsList) {
        Dishes dish = dishMap.get(oi.getDishId());
        Map<String, Object> itemMap = new HashMap<>();
        itemMap.put("dishId", oi.getDishId());
        itemMap.put("name", dish != null ? dish.getName() : ("菜品#" + oi.getDishId()));
        itemMap.put("imageUrl", dish != null ? dish.getImageUrl() : "");
        itemMap.put("price", oi.getUnitPrice());
        itemMap.put("quantity", oi.getQuantity());
        itemMap.put("subtotal", oi.getSubtotal());
        items.add(itemMap);
      }
    }

    // ==================== DELETE 操作 ====================

    // 8. DELETE order_items
    orderItemsMapper.deleteByExample(oiExample);

    // 9. DELETE coupon_usage（如使用优惠券）
    if (hasCoupon) {
      couponUsageMapper.deleteByExample(cuExample);
    }

    // 返回（含删除前的消费明细 + 预约信息，前端可直接显示或"再来一单"）
    SimpleDateFormat dateSdf2 = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat timeSdf2 = new SimpleDateFormat("HH:mm");

    Map<String, Object> result = new HashMap<>();
    result.put("status", "CANCEL_ORDER");
    result.put("refundAmount", refundAmount.intValue());
    result.put("items", items);
    result.put("orderId", orderId);
    // 附带预约信息（从 reservations 记录中提取，供"再来一单"使用）
    result.put("storeId", reservation.getStoreId());
    result.put("tableId", reservation.getTableId());
    if (reservation.getReservationTime() != null) {
      result.put("reserveDate", dateSdf2.format(reservation.getReservationTime()));
      result.put("reserveTime", timeSdf2.format(reservation.getReservationTime()));
    }
    if (reservation.getDurationMin() != null) {
      result.put("duration", reservation.getDurationMin() / 60);
    }
    result.put("persons", reservation.getPartySize());

    // 查询门店和桌位名称
    Stores store = storesMapper.selectByPrimaryKey(reservation.getStoreId());
    if (store != null) {
      result.put("storeName", store.getName() != null ? store.getName() : "");
    }
    Tables table = tablesMapper.selectByPrimaryKey(reservation.getTableId());
    if (table != null) {
      result.put("tableName", table.getTableNo() != null ? table.getTableNo() : "");
      result.put("tableType", table.getTableType() != null ? table.getTableType() : "");
      result.put("tableCapacity", table.getCapacity());
    }
    return result;
  }

  // ==================== E-6: 申请退款 ====================

  @Override
  @Transactional
  public Map<String, Object> applyRefund(Long userId, String orderId) {
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
    if (!canRefundOrder(reservation)) {
      throw new BusinessException(ErrorCode.BAD_REQUEST, "当前订单状态不允许退款");
    }

    Date now = new Date();
    BigDecimal refundAmount = reservation.getOrderAmount() != null
      ? reservation.getOrderAmount()
      : BigDecimal.ZERO;
    int pointsUsed = reservation.getPointsUsed() != null ? reservation.getPointsUsed() : 0;
    int pointsEarned = reservation.getPointsEarned() != null ? reservation.getPointsEarned() : 0;

    // 查询关联支付记录
    PaymentsExample payExample = new PaymentsExample();
    payExample.createCriteria().andReservationIdEqualTo(reservationId);
    List<Payments> payList = paymentsMapper.selectByExample(payExample);
    Payments payment = (payList != null && !payList.isEmpty()) ? payList.get(0) : null;

    // 查询会员扩展信息
    MemberExt memberExt = memberExtMapper.selectByPrimaryKey(userId);

    // ==================== UPDATE 操作 ====================

    // 1. UPDATE reservations: CONFIRMED → REFUNDING（保留金额，审核拒绝时需恢复）
    Reservations updateRes = new Reservations();
    updateRes.setReservationId(reservationId);
    updateRes.setStatus("REFUNDING");
    updateRes.setUpdatedAt(now);
    reservationsMapper.updateByPrimaryKeySelective(updateRes);

    // 2. UPDATE payments: PAID → REFUNDING
    if (payment != null) {
      Payments updatePay = new Payments();
      updatePay.setPaymentId(payment.getPaymentId());
      updatePay.setStatus("REFUNDING");
      paymentsMapper.updateByPrimaryKeySelective(updatePay);
    }

    // 3. UPDATE member_ext: total_points = 原积分 - 获得积分 + 退还使用积分
    //    cumulative_amount = 累计消费 - 退款金额
    if (memberExt != null) {
      int currentPoints = memberExt.getTotalPoints() != null ? memberExt.getTotalPoints() : 0;
      int newPoints = currentPoints - pointsEarned + pointsUsed;
      BigDecimal currentCumulative = memberExt.getCumulativeAmount() != null
        ? memberExt.getCumulativeAmount() : BigDecimal.ZERO;
      BigDecimal newCumulative = currentCumulative.subtract(refundAmount);

      MemberExt updateMe = new MemberExt();
      updateMe.setUserId(userId);
      updateMe.setTotalPoints(newPoints);
      updateMe.setCumulativeAmount(newCumulative);
      memberExtMapper.updateByPrimaryKeySelective(updateMe);

      // ==================== INSERT 操作 ====================

      // 4. INSERT refund_records
      RefundRecords refund = new RefundRecords();
      refund.setPaymentId(payment != null ? payment.getPaymentId() : null);
      refund.setReservationId(reservationId);
      refund.setRefundAmount(refundAmount);
      refund.setRefundReason("用户申请退款");
      refund.setStatus("REQUEST_REFUND");
      refund.setCreatedAt(now);
      refundRecordsMapper.insertSelective(refund);

      // 5. INSERT points_log 记录1：扣除获得的积分（负数）
      if (pointsEarned > 0) {
        PointsLog deductLog = new PointsLog();
        deductLog.setUserId(userId);
        deductLog.setChangeAmount(-pointsEarned);
        deductLog.setBalanceAfter(currentPoints - pointsEarned);
        deductLog.setSource("REFUND_DEDUCT_EARN");
        deductLog.setReservationId(reservationId);
        deductLog.setCreatedAt(now);
        pointsLogMapper.insertSelective(deductLog);
      }

      // 6. INSERT points_log 记录2：退还使用的积分（正数）
      if (pointsUsed > 0) {
        int afterDeduct = currentPoints - pointsEarned;
        PointsLog returnLog = new PointsLog();
        returnLog.setUserId(userId);
        returnLog.setChangeAmount(pointsUsed);
        returnLog.setBalanceAfter(afterDeduct + pointsUsed);
        returnLog.setSource("REFUND_RETURN_USED");
        returnLog.setReservationId(reservationId);
        returnLog.setCreatedAt(now);
        pointsLogMapper.insertSelective(returnLog);
      }
    } else {
      // 无 member_ext 时仅插入退款记录
      RefundRecords refund = new RefundRecords();
      refund.setPaymentId(payment != null ? payment.getPaymentId() : null);
      refund.setReservationId(reservationId);
      refund.setRefundAmount(refundAmount);
      refund.setRefundReason("用户申请退款");
      refund.setStatus("REQUEST_REFUND");
      refund.setCreatedAt(now);
      refundRecordsMapper.insertSelective(refund);
    }

    // 返回（前端检查 code===0 后 loadDetail）
    Map<String, Object> result = new HashMap<>();
    result.put("refundId", reservationId);
    result.put("refundAmount", refundAmount.intValue());
    result.put("status", "REFUNDING");
    return result;
  }

  private static final List<String> ACTIVE_STATUSES = Arrays.asList("BOOKED", "CONFIRMED");

  // ==================== 工具方法 ====================

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
   * 获取该用户最新一条预约记录的 reservationId（用于 insertSelective 后回查主键）
   */
  private Long getLatestReservationId(Long userId) {
    List<Reservations> list = reservationsMapper.selectByUserId(userId);
    if (list != null && !list.isEmpty()) {
      return list.get(0).getReservationId();
    }
    throw new BusinessException(ErrorCode.SERVER_ERROR, "插入预约记录失败");
  }

  /** 取消时限：下单后 5 分钟内可取消（单位：毫秒） */
  private static final long CANCEL_TIME_LIMIT_MS = 5 * 60 * 1000;

  /**
   * 判断订单是否可取消
   * BOOKED（已预约未点单）→ 取消预约，释放桌位（5分钟内）
   * CONFIRMED（已下单/用餐中）→ 取消订单，退款处理（下单后5分钟内）
   */
  private boolean canCancelOrder(Reservations reservation) {
    if (reservation == null || reservation.getStatus() == null) return false;
    String status = reservation.getStatus();
    if (!"BOOKED".equals(status) && !"CONFIRMED".equals(status)) {
      return false;
    }
    // 下单后 5 分钟内可取消
    Date refTime;
    if ("BOOKED".equals(status)) {
      refTime = reservation.getCreatedAt();   // 预约创建时间
    } else {
      refTime = reservation.getUpdatedAt();   // 确认下单时间（submitOrder 时更新）
    }
    if (refTime == null) return false;
    long elapsed = System.currentTimeMillis() - refTime.getTime();
    return elapsed <= CANCEL_TIME_LIMIT_MS;
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
   * 判断订单是否可退款
   * 规则：状态为 confirmed 或 completed 时可以退款
   */
  private boolean canRefundOrder(Reservations reservation) {
    if (reservation == null || reservation.getStatus() == null) return false;
    String status = reservation.getStatus();
    return "CONFIRMED".equals(status) || "COMPLETED".equals(status);
  }

  /**
   * 数据库订单状态 → 前端可识别状态
   *
   * 数据库值 (reservation_status enum):
   *   BOOKED → booked
   *   CONFIRMED → confirmed
   *   OCCUPIED → occupied
   *   COMPLETED → completed
   *   CANCEL_BOOKING / CANCEL_ORDER → cancelled
   *   REFUNDING → 看 refund_records.status 细分（after_sales_pending / after_sales_rejected / after_sales_completed）
   *
   * 前端 ORDER_STATUS_MAP:
   *   booked / confirmed / occupied / completed / cancelled / after_sales_pending / after_sales_rejected / after_sales_completed
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

  /**
   * 前端状态值 → 数据库状态值列表（一对多映射）
   * 用于订单列表的状态筛选
   *
   * 映射关系：
   *   booked    → BOOKED                           （预约未确认）
   *   confirmed → CONFIRMED                        （用餐中）
   *   cancelled → CANCEL_BOOKING, CANCEL_ORDER     （已取消：取消预约 + 取消订单，前端展示时由 mapStatusToFrontend 细分）
   *   afterSales → REFUNDING                       （售后：售后中/拒绝售后/售后完成，由 refund_records 细分）
   *   completed → COMPLETED                        （正常完成）
   */
  private List<String> mapFrontendStatusToDb(String frontendStatus) {
    if (frontendStatus == null) return null;
    switch (frontendStatus) {
      case "booked":
        return java.util.Arrays.asList("BOOKED");
      case "confirmed":
        return java.util.Arrays.asList("CONFIRMED");
      case "cancelled":
        return java.util.Arrays.asList("CANCEL_BOOKING", "CANCEL_ORDER");
      case "afterSales":
        // 售后：退款申请 / 退款完成 / 退款失败，数据库层面都是 REFUNDING
        return java.util.Arrays.asList("REFUNDING");
      case "completed":
        // 仅正常完成
        return java.util.Arrays.asList("COMPLETED");
      default:
        return java.util.Arrays.asList(frontendStatus.toUpperCase());
    }
  }

  /**
   * 前端 tab 状态值 → 允许的前端状态列表（用于二次过滤）
   * 与 mapFrontendStatusToDb 不同：这里返回的是前端展示状态（mapStatusToFrontend 的输出）
   *
   * 映射关系：
   *   booked    → [booked]
   *   confirmed → [confirmed, occupied]
   *   cancelled → [cancel_booking, cancel_order]        （包含 REFUNDING+取消类退款的订单）
   *   afterSales → [after_sales_pending, after_sales_rejected, after_sales_completed] （仅真正的售后）
   *   completed → [completed]
   */
  private List<String> getAllowedFrontendStatuses(String frontendTabStatus) {
    if (frontendTabStatus == null) return null;
    switch (frontendTabStatus) {
      case "booked":
        return java.util.Arrays.asList("booked");
      case "confirmed":
        // 用餐中 tab 包含 occupied
        return java.util.Arrays.asList("confirmed", "occupied");
      case "cancelled":
        // 已取消：cancel_booking + cancel_order（含旧数据 REFUNDING+REQUEST_CANCEL 重映射后的结果）
        return java.util.Arrays.asList("cancel_booking", "cancel_order");
      case "afterSales":
        // 售后：仅真正的售后流程（排除取消订单退款）
        return java.util.Arrays.asList("after_sales_pending", "after_sales_rejected", "after_sales_completed");
      case "completed":
        return java.util.Arrays.asList("completed");
      default:
        return java.util.Arrays.asList(frontendTabStatus.toLowerCase());
    }
  }

  private String mapStatusIcon(String frontendStatus) {
    if (frontendStatus == null) return "📋";
    switch (frontendStatus) {
      case "booked":                  return "📋";
      case "confirmed":               return "🍽️";
      case "occupied":                return "🐱";
      case "completed":               return "✅";
      case "cancel_booking":          return "❌";
      case "cancel_order":            return "❌";
      case "after_sales_pending":     return "⏳";
      case "after_sales_rejected":    return "⚠️";
      case "after_sales_completed":   return "💰";
      default:                        return "📋";
    }
  }

  private String mapStatusDesc(String frontendStatus) {
    if (frontendStatus == null) return "订单处理中";
    switch (frontendStatus) {
      case "booked":                  return "预约成功，请准时到店";
      case "confirmed":               return "顾客正在用餐中";
      case "occupied":                return "顾客已到店，正在享受猫咪时光";
      case "completed":               return "期待您的评价";
      case "cancel_booking":          return "预约已取消";
      case "cancel_order":            return "订单已取消";
      case "after_sales_pending":     return "售后处理中";
      case "after_sales_rejected":    return "售后已被拒绝";
      case "after_sales_completed":   return "售后已完成";
      default:                        return "订单处理中";
    }
  }
}
