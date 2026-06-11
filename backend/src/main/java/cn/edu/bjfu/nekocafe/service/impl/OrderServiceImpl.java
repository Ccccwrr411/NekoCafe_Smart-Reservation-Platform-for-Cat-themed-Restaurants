package cn.edu.bjfu.nekocafe.service.impl;

import cn.edu.bjfu.nekocafe.common.ErrorCode;
import cn.edu.bjfu.nekocafe.dto.OrderSubmitDTO;
import cn.edu.bjfu.nekocafe.dto.ReservationCreateDTO;
import cn.edu.bjfu.nekocafe.dto.RescheduleDTO;
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

  // ==================== E-1: 订单列表 ====================

  @Override
  public List<OrderVO> listOrders(Long userId) {
    // 1. 查该用户所有预约
    ReservationsExample example = new ReservationsExample();
    example.createCriteria().andUserIdEqualTo(userId);
    example.setOrderByClause("reservation_time DESC");
    List<Reservations> reservationsList = reservationsMapper.selectByExample(example);

    if (reservationsList == null || reservationsList.isEmpty()) {
      return new ArrayList<>();
    }

    // 收集所有 reservationId
    List<Long> reservationIds = new ArrayList<>();
    for (Reservations r : reservationsList) {
      reservationIds.add(r.getReservationId());
    }

    // 2. 批量查 OrderItems
    OrderItemsExample itemsExample = new OrderItemsExample();
    itemsExample.createCriteria().andReservationIdIn(reservationIds);
    List<OrderItems> allItems = orderItemsMapper.selectByExample(itemsExample);

    // 按 reservationId 分组
    Map<Long, List<OrderItems>> itemsMap = new HashMap<>();
    for (OrderItems item : allItems) {
      itemsMap.computeIfAbsent(item.getReservationId(), k -> new ArrayList<>()).add(item);
    }

    // 3. 批量查门店（用于 storeName）
    Set<Integer> storeIds = new HashSet<>();
    for (Reservations r : reservationsList) {
      storeIds.add(r.getStoreId());
    }
    Map<Integer, String> storeNameMap = new HashMap<>();
    if (!storeIds.isEmpty()) {
      StoresExample storesExample = new StoresExample();
      storesExample.createCriteria().andStoreIdIn(new ArrayList<>(storeIds));
      List<Stores> storesList = storesMapper.selectByExample(storesExample);
      for (Stores s : storesList) {
        storeNameMap.put(s.getStoreId(), s.getName());
      }
    }

    // 4. 批量查桌位（用于 tableName）
    Set<Integer> tableIds = new HashSet<>();
    for (Reservations r : reservationsList) {
      if (r.getTableId() != null) {
        tableIds.add(r.getTableId());
      }
    }
    Map<Integer, String> tableNameMap = new HashMap<>();
    if (!tableIds.isEmpty()) {
      TablesExample tablesExample = new TablesExample();
      tablesExample.createCriteria().andTableIdIn(new ArrayList<>(tableIds));
      List<Tables> tablesList = tablesMapper.selectByExample(tablesExample);
      for (Tables t : tablesList) {
        tableNameMap.put(t.getTableId(), t.getTableNo());
      }
    }

    // 5. 批量查支付记录（用于 payTime / payType）
    PaymentsExample payExample = new PaymentsExample();
    payExample.createCriteria().andReservationIdIn(reservationIds);
    List<Payments> allPayments = paymentsMapper.selectByExample(payExample);
    Map<Long, Payments> paymentsMap = new HashMap<>();
    for (Payments p : allPayments) {
      paymentsMap.put(p.getReservationId(), p);
    }

    // 6. 拼装 OrderVO 列表
    List<OrderVO> result = new ArrayList<>();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat dateSdf = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat timeSdf = new SimpleDateFormat("HH:mm");

    for (Reservations r : reservationsList) {
      OrderVO vo = new OrderVO();
      vo.setId(formatOrderId(r.getReservationId()));
      vo.setStoreId(r.getStoreId());
      vo.setStoreName(storeNameMap.getOrDefault(r.getStoreId(), ""));
      vo.setTableId(r.getTableId());
      vo.setTableName(tableNameMap.getOrDefault(r.getTableId(), ""));

      if (r.getReservationTime() != null) {
        vo.setReserveDate(dateSdf.format(r.getReservationTime()));
        vo.setReserveTime(timeSdf.format(r.getReservationTime()));
      }
      if (r.getDurationMin() != null) {
        vo.setDuration(r.getDurationMin() / 60);
      }
      vo.setPersons(r.getPartySize());
      vo.setStatus(r.getStatus());
      vo.setRemark(r.getSpecialRequest() != null ? r.getSpecialRequest() : "");

      // 金额从 BigDecimal 转 Integer（分）
      if (r.getTotalAmount() != null) {
        vo.setTotalAmount(r.getTotalAmount().intValue());
      }
      if (r.getOrderAmount() != null) {
        // discountAmount = totalAmount - orderAmount（简单估算）
        int discount = r.getTotalAmount().intValue() - r.getOrderAmount().intValue();
        vo.setDiscountAmount(Math.max(0, discount));
        vo.setFinalAmount(r.getOrderAmount().intValue());
      }

      if (r.getCreatedAt() != null) {
        vo.setCreateTime(sdf.format(r.getCreatedAt()));
      }

      // 填充支付信息
      Payments payment = paymentsMap.get(r.getReservationId());
      if (payment != null) {
        if (payment.getPaidAt() != null) {
          vo.setPayTime(sdf.format(payment.getPaidAt()));
        }
        vo.setPayType(payment.getPaymentMethod() != null ? payment.getPaymentMethod() : "");
      } else {
        vo.setPayTime("");
        vo.setPayType("");
      }

      // 填充 items
      List<OrderItems> items = itemsMap.getOrDefault(r.getReservationId(), new ArrayList<>());
      List<OrderVO.OrderItemVO> itemVOs = new ArrayList<>();
      for (OrderItems item : items) {
        OrderVO.OrderItemVO itemVO = new OrderVO.OrderItemVO();
        itemVO.setName("菜品" + item.getDishId()); // dishId 到菜名可通过 MenuMapper 查，这里用简单方式
        itemVO.setQty(item.getQuantity());
        if (item.getUnitPrice() != null) {
          itemVO.setPrice(item.getUnitPrice().intValue());
        }
        itemVOs.add(itemVO);
      }
      vo.setItems(itemVOs);

      // 填充 timeline（简易）
      List<OrderVO.TimelineVO> timeline = new ArrayList<>();
      if (r.getCreatedAt() != null) {
        OrderVO.TimelineVO t = new OrderVO.TimelineVO();
        t.setTime(sdf.format(r.getCreatedAt()));
        t.setTitle("订单创建");
        t.setDesc("订单已创建");
        timeline.add(t);
      }
      vo.setTimeline(timeline);

      // canCancel / canReschedule / canRefund
      vo.setCanCancel(canCancelOrder(r));
      vo.setCanReschedule(canRescheduleOrder(r));
      vo.setCanRefund(canRefundOrder(r));
      vo.setHasReview(false); // 评价由 ReviewService 处理

      result.add(vo);
    }

    return result;
  }

  // ==================== E-2: 提交订单 ====================

  @Override
  @Transactional
  public Map<String, Object> submitOrder(Long userId, OrderSubmitDTO dto) {
    // 1. 参数校验
    if (dto.getStoreId() == null || dto.getTableId() == null) {
      throw new BusinessException(ErrorCode.BAD_REQUEST, "门店ID和桌位ID不能为空");
    }

    // 2. 防并发超卖：乐观锁占用桌位
    // 先用 UPDATE ... WHERE is_active = true 抢占，通过 affected rows 判断是否成功
    Tables updateTable = new Tables();
    updateTable.setIsActive(false);
    TablesExample tableExample = new TablesExample();
    tableExample.createCriteria()
      .andTableIdEqualTo(dto.getTableId())
      .andIsActiveEqualTo(true);
    int affected = tablesMapper.updateByExampleSelective(updateTable, tableExample);
    if (affected == 0) {
      throw new BusinessException(ErrorCode.NOT_FOUND, "桌位不存在或已被占用");
    }

    // 3. 计算金额
    int totalAmount = dto.getTotalAmount() != null ? dto.getTotalAmount() : 0;
    int finalAmount = dto.getFinalAmount() != null ? dto.getFinalAmount() : totalAmount;
    int discount = dto.getDiscount() != null ? dto.getDiscount() : 0;

    // 4. 插入 Reservations 记录
    Reservations reservation = new Reservations();
    reservation.setUserId(userId);
    reservation.setStoreId(dto.getStoreId());
    reservation.setTableId(dto.getTableId());
    reservation.setReservationTime(new Date());
    reservation.setDurationMin(120); // 默认2小时
    reservation.setPartySize(1);
    reservation.setSpecialRequest(dto.getRemark());
    reservation.setTotalAmount(new BigDecimal(totalAmount));
    reservation.setOrderAmount(new BigDecimal(finalAmount));
    reservation.setStatus("confirmed");
    reservation.setCreatedAt(new Date());
    reservation.setUpdatedAt(new Date());

    reservationsMapper.insertSelective(reservation);
    // insertSelective 不会回填自增主键，查回最新记录获取 reservationId
    Long reservationId = getLatestReservationId(userId);

    // 5. 批量插入 OrderItems
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

    // 6. 插入 Payments 记录（mock 支付）
    Payments payment = new Payments();
    payment.setReservationId(reservationId);
    payment.setPaymentMethod("wechat");
    payment.setAmount(new BigDecimal(finalAmount));
    payment.setTransactionId("MOCK_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
    payment.setStatus("paid");
    payment.setPaidAt(new Date());
    payment.setCreatedAt(new Date());
    paymentsMapper.insertSelective(payment);

    // 7. 构造返回
    Map<String, Object> result = new HashMap<>();
    result.put("orderId", formatOrderId(reservationId));
    result.put("totalAmount", totalAmount);
    result.put("finalAmount", finalAmount);
    Map<String, String> payInfo = new HashMap<>();
    payInfo.put("transactionId", payment.getTransactionId());
    payInfo.put("method", "wechat");
    payInfo.put("status", "paid");
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

    // 查 OrderItems
    OrderItemsExample itemsExample = new OrderItemsExample();
    itemsExample.createCriteria().andReservationIdEqualTo(reservationId);
    List<OrderItems> items = orderItemsMapper.selectByExample(itemsExample);

    // 查门店名
    Stores store = storesMapper.selectByPrimaryKey(reservation.getStoreId());
    String storeName = store != null ? store.getName() : "";

    // 查桌位名
    Tables table = tablesMapper.selectByPrimaryKey(reservation.getTableId());
    String tableName = table != null ? table.getTableNo() : "";

    // 查支付记录
    PaymentsExample payExample = new PaymentsExample();
    payExample.createCriteria().andReservationIdEqualTo(reservationId);
    List<Payments> paymentsList = paymentsMapper.selectByExample(payExample);
    Payments payment = (paymentsList != null && !paymentsList.isEmpty()) ? paymentsList.get(0) : null;

    // 拼装 OrderVO
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat dateSdf = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat timeSdf = new SimpleDateFormat("HH:mm");

    OrderVO vo = new OrderVO();
    vo.setId(formatOrderId(reservationId));
    vo.setStoreId(reservation.getStoreId());
    vo.setStoreName(storeName);
    vo.setTableId(reservation.getTableId());
    vo.setTableName(tableName);

    if (reservation.getReservationTime() != null) {
      vo.setReserveDate(dateSdf.format(reservation.getReservationTime()));
      vo.setReserveTime(timeSdf.format(reservation.getReservationTime()));
    }
    if (reservation.getDurationMin() != null) {
      vo.setDuration(reservation.getDurationMin() / 60);
    }
    vo.setPersons(reservation.getPartySize());
    vo.setStatus(reservation.getStatus());
    vo.setRemark(reservation.getSpecialRequest() != null ? reservation.getSpecialRequest() : "");

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

    // 支付信息
    if (payment != null) {
      if (payment.getPaidAt() != null) {
        vo.setPayTime(sdf.format(payment.getPaidAt()));
      }
      vo.setPayType(payment.getPaymentMethod() != null ? payment.getPaymentMethod() : "");
    } else {
      vo.setPayTime("");
      vo.setPayType("");
    }

    // items
    List<OrderVO.OrderItemVO> itemVOs = new ArrayList<>();
    for (OrderItems item : items) {
      OrderVO.OrderItemVO itemVO = new OrderVO.OrderItemVO();
      itemVO.setName("菜品" + item.getDishId());
      itemVO.setQty(item.getQuantity());
      if (item.getUnitPrice() != null) {
        itemVO.setPrice(item.getUnitPrice().intValue());
      }
      itemVOs.add(itemVO);
    }
    vo.setItems(itemVOs);

    // timeline
    List<OrderVO.TimelineVO> timeline = new ArrayList<>();
    if (reservation.getCreatedAt() != null) {
      OrderVO.TimelineVO t = new OrderVO.TimelineVO();
      t.setTime(sdf.format(reservation.getCreatedAt()));
      t.setTitle("订单创建");
      t.setDesc("订单已创建");
      timeline.add(t);
    }
    vo.setTimeline(timeline);

    vo.setCanCancel(canCancelOrder(reservation));
    vo.setCanReschedule(canRescheduleOrder(reservation));
    vo.setCanRefund(canRefundOrder(reservation));
    vo.setHasReview(false);

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
    // 用 String 比较避免 Long 对象 equals null 的问题
    if (userId == null || reservation.getUserId() == null
      || !reservation.getUserId().toString().equals(userId.toString())) {
      throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作此订单");
    }
    if (!canCancelOrder(reservation)) {
      throw new BusinessException(ErrorCode.BAD_REQUEST, "当前订单状态不允许取消");
    }

    // 更新状态
    reservation.setStatus("cancelled");
    reservation.setUpdatedAt(new Date());
    reservationsMapper.updateByPrimaryKeySelective(reservation);

    // 创建退款记录
    BigDecimal refundAmount = reservation.getOrderAmount() != null
      ? reservation.getOrderAmount()
      : BigDecimal.ZERO;

    // 查询关联的 payment_id
    Long paymentId = null;
    PaymentsExample payExample = new PaymentsExample();
    payExample.createCriteria().andReservationIdEqualTo(reservationId);
    List<Payments> payList = paymentsMapper.selectByExample(payExample);
    if (payList != null && !payList.isEmpty()) {
      paymentId = payList.get(0).getPaymentId();
    }

    RefundRecords refund = new RefundRecords();
    refund.setReservationId(reservationId);
    refund.setPaymentId(paymentId);
    refund.setRefundAmount(refundAmount);
    refund.setRefundReason("用户取消订单");
    refund.setStatus("completed");
    refund.setCreatedAt(new Date());
    refund.setCompletedAt(new Date());
    refundRecordsMapper.insertSelective(refund);

    Map<String, Object> result = new HashMap<>();
    result.put("status", "cancelled");
    result.put("refundAmount", refundAmount.intValue());
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

    // 解析新的预约时间
    try {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
      Date newReserveTime = sdf.parse(dto.getNewReserveDate() + " " + dto.getNewReserveTime());
      reservation.setReservationTime(newReserveTime);
    } catch (Exception e) {
      throw new BusinessException(ErrorCode.BAD_REQUEST, "预约时间格式错误");
    }

    reservation.setUpdatedAt(new Date());
    reservationsMapper.updateByPrimaryKeySelective(reservation);

    Map<String, Object> result = new HashMap<>();
    result.put("orderId", dto.getOrderId());
    result.put("newReserveDate", dto.getNewReserveDate());
    result.put("newReserveTime", dto.getNewReserveTime());
    result.put("status", "rescheduled");
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

    // 创建退款记录
    BigDecimal refundAmount = reservation.getOrderAmount() != null
      ? reservation.getOrderAmount()
      : BigDecimal.ZERO;

    // 查询关联的 payment_id
    Long paymentId = null;
    PaymentsExample payExample = new PaymentsExample();
    payExample.createCriteria().andReservationIdEqualTo(reservationId);
    List<Payments> payList = paymentsMapper.selectByExample(payExample);
    if (payList != null && !payList.isEmpty()) {
      paymentId = payList.get(0).getPaymentId();
    }

    RefundRecords refund = new RefundRecords();
    refund.setReservationId(reservationId);
    refund.setPaymentId(paymentId);
    refund.setRefundAmount(refundAmount);
    refund.setRefundReason("用户申请退款");
    refund.setStatus("processing");
    refund.setCreatedAt(new Date());
    refundRecordsMapper.insertSelective(refund);

    // 更新订单状态为退款中
    reservation.setStatus("refunding");
    reservation.setUpdatedAt(new Date());
    reservationsMapper.updateByPrimaryKeySelective(reservation);

    Map<String, Object> result = new HashMap<>();
    result.put("refundId", refund.getRefundId());
    result.put("refundAmount", refundAmount.intValue());
    result.put("status", "processing");
    return result;
  }

  // ==================== E-7: 纯预约（创建预约，无点单） ====================

  @Override
  @Transactional
  public Map<String, Object> createReservation(Long userId, ReservationCreateDTO dto) {
    // 1. 参数校验
    if (dto.getStoreId() == null || dto.getTableId() == null) {
      throw new BusinessException(ErrorCode.BAD_REQUEST, "门店ID和桌位ID不能为空");
    }

    // 2. 防并发超卖：乐观锁占用桌位
    Tables updateTable = new Tables();
    updateTable.setIsActive(false);
    TablesExample tableExample = new TablesExample();
    tableExample.createCriteria()
      .andTableIdEqualTo(dto.getTableId())
      .andIsActiveEqualTo(true);
    int affected = tablesMapper.updateByExampleSelective(updateTable, tableExample);
    if (affected == 0) {
      throw new BusinessException(ErrorCode.NOT_FOUND, "桌位不存在或已被占用");
    }

    // 3. 解析预约时间
    Date reserveTime;
    try {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
      reserveTime = sdf.parse(dto.getReserveDate() + " " + dto.getReserveTime());
    } catch (Exception e) {
      throw new BusinessException(ErrorCode.BAD_REQUEST, "预约时间格式错误");
    }

    // 4. INSERT Reservations 记录（无点单，items 为空）
    Reservations reservation = new Reservations();
    reservation.setUserId(userId);
    reservation.setStoreId(dto.getStoreId());
    reservation.setTableId(dto.getTableId());
    reservation.setReservationTime(reserveTime);
    reservation.setDurationMin(dto.getDuration() != null ? dto.getDuration() * 60 : 120); // 小时转分钟
    reservation.setPartySize(dto.getPersons() != null ? dto.getPersons() : 1);
    reservation.setTotalAmount(BigDecimal.ZERO);
    reservation.setOrderAmount(BigDecimal.ZERO);
    reservation.setStatus("confirmed");
    reservation.setCreatedAt(new Date());
    reservation.setUpdatedAt(new Date());

    reservationsMapper.insertSelective(reservation);
    Long reservationId = getLatestReservationId(userId);

    Map<String, Object> result = new HashMap<>();
    result.put("orderId", formatOrderId(reservationId));
    result.put("status", "confirmed");
    return result;
  }

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
    ReservationsExample example = new ReservationsExample();
    example.createCriteria().andUserIdEqualTo(userId);
    example.setOrderByClause("created_at DESC");
    List<Reservations> list = reservationsMapper.selectByExample(example);
    if (list != null && !list.isEmpty()) {
      return list.get(0).getReservationId();
    }
    throw new BusinessException(ErrorCode.SERVER_ERROR, "插入预约记录失败");
  }

  /**
   * 判断订单是否可取消
   * 规则：状态为 confirmed 或 pending 时可以取消
   */
  private boolean canCancelOrder(Reservations reservation) {
    if (reservation == null || reservation.getStatus() == null) return false;
    String status = reservation.getStatus();
    return "confirmed".equals(status) || "pending".equals(status);
  }

  /**
   * 判断订单是否可改约
   * 规则：状态为 confirmed 或 pending 时可以改约
   */
  private boolean canRescheduleOrder(Reservations reservation) {
    if (reservation == null || reservation.getStatus() == null) return false;
    String status = reservation.getStatus();
    return "confirmed".equals(status) || "pending".equals(status);
  }

  /**
   * 判断订单是否可退款
   * 规则：状态为 confirmed 或 completed 时可以退款
   */
  private boolean canRefundOrder(Reservations reservation) {
    if (reservation == null || reservation.getStatus() == null) return false;
    String status = reservation.getStatus();
    return "confirmed".equals(status) || "completed".equals(status);
  }
}
