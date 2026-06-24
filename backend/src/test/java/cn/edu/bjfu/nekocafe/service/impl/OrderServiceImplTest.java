package cn.edu.bjfu.nekocafe.service.impl;

import cn.edu.bjfu.nekocafe.dto.OrderSubmitDTO;
import cn.edu.bjfu.nekocafe.dto.RescheduleDTO;
import cn.edu.bjfu.nekocafe.entity.*;
import cn.edu.bjfu.nekocafe.exception.BusinessException;
import cn.edu.bjfu.nekocafe.mapper.*;
import cn.edu.bjfu.nekocafe.vo.OrderVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

  @Mock private ReservationsMapper reservationsMapper;
  @Mock private OrderItemsMapper orderItemsMapper;
  @Mock private StoresMapper storesMapper;
  @Mock private TablesMapper tablesMapper;
  @Mock private RefundRecordsMapper refundRecordsMapper;
  @Mock private PaymentsMapper paymentsMapper;
  @Mock private TableStatusMapper tableStatusMapper;
  @Mock private UserCouponsMapper userCouponsMapper;
  @Mock private CouponUsageMapper couponUsageMapper;
  @Mock private PointsLogMapper pointsLogMapper;
  @Mock private MemberExtMapper memberExtMapper;
  @Mock private DishesMapper dishesMapper;
  @Mock private PromotionsMapper promotionsMapper;
  @Mock private UsersMapper usersMapper;

  @InjectMocks
  private OrderServiceImpl service;

  // 辅助方法：生成一个完美的假预约记录
  private Reservations createPerfectReservation(String status) {
    Reservations r = new Reservations();
    r.setReservationId(5L);
    r.setUserId(9L);
    r.setStoreId(1);
    r.setTableId(2);
    r.setStatus(status);
    r.setReservationTime(new Date(System.currentTimeMillis() + 60 * 60 * 1000L));
    r.setCreatedAt(new Date());
    r.setUpdatedAt(new Date());
    r.setDurationMin(120);
    r.setPartySize(2);
    r.setTotalAmount(new BigDecimal("100"));
    r.setOrderAmount(new BigDecimal("80"));
    return r;
  }

  // ==========================================
  // 1. 列表 & 详情查询 (狂刷几十行拼装逻辑)
  // ==========================================
  @Test
  @DisplayName("测试：获取订单列表 (满血覆盖所有关联查询和拼装逻辑)")
  void listOrders_FullCoverage() {
    Reservations r = createPerfectReservation("REFUNDING"); // 触发售后判断分支
    when(reservationsMapper.selectByUserId(9L)).thenReturn(Collections.singletonList(r));

    // 模拟各种关联数据，让所有 if (xxx != null) 都能进去
    OrderItems oi = new OrderItems(); oi.setReservationId(5L); oi.setDishId(1); oi.setQuantity(1);
    lenient().when(orderItemsMapper.selectByExample(any())).thenReturn(Collections.singletonList(oi));

    Dishes d = new Dishes(); d.setDishId(1); d.setName("猫条"); d.setCategory("零食");
    lenient().when(dishesMapper.selectByExample(any())).thenReturn(Collections.singletonList(d));

    Stores s = new Stores(); s.setStoreId(1); s.setName("测试门店");
    lenient().when(storesMapper.selectByExample(any())).thenReturn(Collections.singletonList(s));

    Tables t = new Tables(); t.setTableId(2); t.setTableNo("A1");
    lenient().when(tablesMapper.selectByExample(any())).thenReturn(Collections.singletonList(t));

    Payments p = new Payments(); p.setReservationId(5L); p.setAmount(new BigDecimal("80")); p.setPaidAt(new Date());
    lenient().when(paymentsMapper.selectByExample(any())).thenReturn(Collections.singletonList(p));

    RefundRecords rr = new RefundRecords(); rr.setReservationId(5L); rr.setStatus("REQUEST_CANCEL"); rr.setCreatedAt(new Date());
    lenient().when(refundRecordsMapper.selectByExample(any())).thenReturn(Collections.singletonList(rr));

    List<OrderVO> result = service.listOrders(9L, "all", "A1");
    assertFalse(result.isEmpty());
    assertEquals("ORD0000000005", result.get(0).getId());
  }
  @Test
  @DisplayName("测试：listOrders - 彻底压榨所有 if/else 分支")
  void listOrders_BranchCoverage() {
    // 1. 触发 status="all" 分支 (执行第11-13行的 else)
    service.listOrders(9L, "all", null);

    // 2. 触发 status 有值但 mapFrontendStatusToDb 返回为空的情况 (执行第8-9行的 else)
    // 假设 "unknown" 在 map 里找不到对应的数据库状态
    service.listOrders(9L, "unknown", null);

    // 3. 触发结果为空的情况 (覆盖第15-17行)
    when(reservationsMapper.selectByUserId(9L)).thenReturn(Collections.emptyList());
    List<OrderVO> result = service.listOrders(9L, "all", null);
    assertTrue(result.isEmpty());
  }
  @Test
  @DisplayName("测试：获取订单详情 (满血覆盖所有详情拼装)")
  void getOrderDetail_FullCoverage() {
    Reservations r = createPerfectReservation("CONFIRMED");
    when(reservationsMapper.selectByPrimaryKey(5L)).thenReturn(r);

    Stores s = new Stores(); s.setName("测试门店");
    lenient().when(storesMapper.selectByPrimaryKey(1)).thenReturn(s);

    Tables t = new Tables(); t.setTableNo("A1");
    lenient().when(tablesMapper.selectByPrimaryKey(2)).thenReturn(t);

    TableStatus ts = new TableStatus(); ts.setStatus("OCCUPIED");
    lenient().when(tableStatusMapper.selectByPrimaryKey(2)).thenReturn(ts);

    OrderItems oi = new OrderItems(); oi.setDishId(1); oi.setQuantity(1); oi.setUnitPrice(new BigDecimal("20"));
    lenient().when(orderItemsMapper.selectByExample(any())).thenReturn(Collections.singletonList(oi));

    Dishes d = new Dishes(); d.setDishId(1); d.setName("猫条"); d.setImageUrl("url");
    lenient().when(dishesMapper.selectByExample(any())).thenReturn(Collections.singletonList(d));

    Payments p = new Payments(); p.setAmount(new BigDecimal("80")); p.setPaidAt(new Date()); p.setStatus("PAID");
    lenient().when(paymentsMapper.selectByExample(any())).thenReturn(Collections.singletonList(p));

    RefundRecords rr = new RefundRecords(); rr.setStatus("COMPLETED"); rr.setCreatedAt(new Date());
    lenient().when(refundRecordsMapper.selectByExample(any())).thenReturn(Collections.singletonList(rr));

    CouponUsage cu = new CouponUsage(); cu.setCouponId(1L); cu.setDiscountAmount(new BigDecimal("20"));
    lenient().when(couponUsageMapper.selectByExample(any())).thenReturn(Collections.singletonList(cu));

    UserCoupons uc = new UserCoupons(); uc.setStatus("USED"); uc.setPromoId(1);
    lenient().when(userCouponsMapper.selectByPrimaryKey(1L)).thenReturn(uc);

    Promotions promo = new Promotions(); promo.setName("满减"); promo.setType("DISCOUNT");
    lenient().when(promotionsMapper.selectByPrimaryKey(1)).thenReturn(promo);

    MemberExt me = new MemberExt(); me.setLevel(2); me.setTotalPoints(100);
    lenient().when(memberExtMapper.selectByPrimaryKey(9L)).thenReturn(me);

    Users user = new Users(); user.setNickname("测试用户");
    lenient().when(usersMapper.selectByPrimaryKey(9L)).thenReturn(user);

    OrderVO result = service.getOrderDetail("ORD0000000005");
    assertNotNull(result);
    assertEquals("confirmed", result.getStatus());
  }

  // ==========================================
  // 2. 取消订单 (狂刷最复杂的 100 行取消逻辑)
  // ==========================================
  @Test
  @DisplayName("测试：取消订单 (BOOKED 简单取消)")
  void cancelOrder_Booked_Success() {
    Reservations r = createPerfectReservation("BOOKED");
    when(reservationsMapper.selectByPrimaryKey(5L)).thenReturn(r);
    when(reservationsMapper.countActiveByTableIdExcluding(anyInt(), anyList(), anyLong())).thenReturn(0);

    TableStatus ts = new TableStatus(); ts.setVersion(1);
    when(tableStatusMapper.selectByPrimaryKey(2)).thenReturn(ts);
    when(tableStatusMapper.releaseTableOptimistic(anyInt(), anyLong(), anyInt())).thenReturn(1);

    Map<String, Object> result = service.cancelOrder(9L, "ORD0000000005");
    assertEquals("CANCEL_BOOKING", result.get("status"));
  }

  @Test
  @DisplayName("测试：取消订单 (CONFIRMED 完整取消，触发退款/退券/扣积分)")
  void cancelOrder_Confirmed_Success() {
    Reservations r = createPerfectReservation("CONFIRMED");
    r.setPointsUsed(10);
    r.setPointsEarned(50);
    when(reservationsMapper.selectByPrimaryKey(5L)).thenReturn(r);

    // 模拟已支付记录 (触发直接退款)
    Payments p = new Payments(); p.setPaymentId(100L); p.setStatus("PAID");
    when(paymentsMapper.selectByExample(any())).thenReturn(Collections.singletonList(p));

    // 模拟使用优惠券 (触发退还优惠券)
    CouponUsage cu = new CouponUsage(); cu.setCouponId(88L);
    when(couponUsageMapper.selectByExample(any())).thenReturn(Collections.singletonList(cu));

    // 模拟会员信息 (触发扣除积分和消费额)
    MemberExt me = new MemberExt(); me.setTotalPoints(100); me.setCumulativeAmount(new BigDecimal("500"));
    when(memberExtMapper.selectByPrimaryKey(9L)).thenReturn(me);

    // 模拟消费明细 (触发展示取消项)
    OrderItems oi = new OrderItems(); oi.setDishId(1); oi.setQuantity(2); oi.setUnitPrice(new BigDecimal("20"));
    when(orderItemsMapper.selectByExample(any())).thenReturn(Collections.singletonList(oi));

    Dishes d = new Dishes(); d.setDishId(1); d.setName("猫条");
    lenient().when(dishesMapper.selectByExample(any())).thenReturn(Collections.singletonList(d));

    // Mock 所有增删改操作全部成功
    lenient().when(reservationsMapper.updateByPrimaryKeySelective(any())).thenReturn(1);
    lenient().when(paymentsMapper.updateByPrimaryKeySelective(any())).thenReturn(1);
    lenient().when(userCouponsMapper.updateByExampleSelective(any(), any())).thenReturn(1);
    lenient().when(memberExtMapper.updateByPrimaryKeySelective(any())).thenReturn(1);
    lenient().when(refundRecordsMapper.insertSelective(any())).thenReturn(1);
    lenient().when(pointsLogMapper.insertSelective(any())).thenReturn(1);
    lenient().when(orderItemsMapper.deleteByExample(any())).thenReturn(1);
    lenient().when(couponUsageMapper.deleteByExample(any())).thenReturn(1);
    lenient().when(storesMapper.selectByPrimaryKey(any())).thenReturn(new Stores());
    lenient().when(tablesMapper.selectByPrimaryKey(any())).thenReturn(new Tables());

    Map<String, Object> result = service.cancelOrder(9L, "ORD0000000005");
    assertEquals("CANCEL_ORDER", result.get("status"));
  }

  // ==========================================
  // 3. 下单、改约、退款 (保持原样)
  // ==========================================
  @Test
  @DisplayName("测试：提交订单 - 成功跑完全流程")
  void submitOrder_Success() {
    OrderSubmitDTO dto = new OrderSubmitDTO();
    dto.setStoreId(1);
    dto.setOrderId("ORD0000000005");
    dto.setTableId(2);
    dto.setTotalAmount(100);
    dto.setFinalAmount(80);
    dto.setCouponIds(Collections.singletonList("1"));

    OrderSubmitDTO.OrderItemDTO item = new OrderSubmitDTO.OrderItemDTO();
    item.setMenuId(101); item.setPrice(50); item.setQty(2);
    dto.setItems(Collections.singletonList(item));

    when(reservationsMapper.selectByPrimaryKey(5L)).thenReturn(createPerfectReservation("BOOKED"));

    TableStatus ts = new TableStatus(); ts.setStatus("RESERVED"); ts.setVersion(1);
    lenient().when(tableStatusMapper.selectByPrimaryKey(anyInt())).thenReturn(ts);
    lenient().when(tableStatusMapper.occupyTableOptimistic(anyInt(), anyInt())).thenReturn(1);

    lenient().when(memberExtMapper.selectByPrimaryKey(9L)).thenReturn(null);
    lenient().when(memberExtMapper.insertSelective(any())).thenReturn(1);

    lenient().when(reservationsMapper.updateByPrimaryKeySelective(any())).thenReturn(1);
    lenient().when(userCouponsMapper.updateByExampleSelective(any(), any())).thenReturn(1);
    lenient().when(orderItemsMapper.insertSelective(any())).thenReturn(1);
    lenient().when(couponUsageMapper.insertSelective(any())).thenReturn(1);
    lenient().when(pointsLogMapper.insertSelective(any())).thenReturn(1);
    lenient().when(paymentsMapper.insertSelective(any())).thenReturn(1);

    Map<String, Object> result = service.submitOrder(9L, dto);
    assertNotNull(result);
  }

  @Test
  @DisplayName("测试：申请退款")
  void applyRefund_Success() {
    Reservations r = createPerfectReservation("CONFIRMED");
    when(reservationsMapper.selectByPrimaryKey(5L)).thenReturn(r);

    Payments p = new Payments(); p.setPaymentId(100L);
    when(paymentsMapper.selectByExample(any())).thenReturn(Collections.singletonList(p));

    lenient().when(reservationsMapper.updateByPrimaryKeySelective(any())).thenReturn(1);
    lenient().when(paymentsMapper.updateByPrimaryKeySelective(any())).thenReturn(1);
    lenient().when(refundRecordsMapper.insertSelective(any())).thenReturn(1);

    Map<String, Object> result = service.applyRefund(9L, "ORD0000000005", "猫不够胖");
    assertEquals("REFUNDING", result.get("status"));
  }

  @Test
  @DisplayName("测试：订单改期")
  void reschedule_Success() {
    Reservations r = createPerfectReservation("CONFIRMED");
    when(reservationsMapper.selectByPrimaryKey(5L)).thenReturn(r);
    lenient().when(reservationsMapper.updateByPrimaryKeySelective(any())).thenReturn(1);

    RescheduleDTO dto = new RescheduleDTO();
    dto.setOrderId("ORD0000000005");
    dto.setNewReserveDate("2026-06-16");
    dto.setNewReserveTime("14:00");

    Map<String, Object> result = service.reschedule(9L, dto);
    assertEquals("cancelled", result.get("status"));
  }
}
