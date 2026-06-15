package cn.edu.bjfu.nekocafe.service.impl;

import cn.edu.bjfu.nekocafe.entity.*;
import cn.edu.bjfu.nekocafe.mapper.*;
import cn.edu.bjfu.nekocafe.service.NotificationService;
import cn.edu.bjfu.nekocafe.vo.DashboardMetricsVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StaffServiceImplTest {

  @Mock private StoreDailyStatsMapper storeDailyStatsMapper;
  @Mock private TableStatusMapper tableStatusMapper;
  @Mock private TablesMapper tablesMapper;
  @Mock private ReservationsMapper reservationsMapper;
  @Mock private ShiftExceptionsMapper shiftExceptionsMapper;
  @Mock private RefundRecordsMapper refundRecordsMapper;
  @Mock private PaymentsMapper paymentsMapper;
  @Mock private MemberExtMapper memberExtMapper;
  @Mock private PointsLogMapper pointsLogMapper;
  @Mock private NotificationService notificationService;

  @InjectMocks
  private StaffServiceImpl service;

  // ==========================================
  // 1. 数据看板测试 (触发无数据时的 Mock 构建逻辑)
  // ==========================================
  @Test
  @DisplayName("测试：获取数据看板 - 触发 Mock 兜底数据")
  void getDashboardMetrics_MockData() {
    // 模拟数据库没有查到统计数据，触发 buildMockStats 逻辑
    lenient().when(storeDailyStatsMapper.selectByExample(any())).thenReturn(Collections.emptyList());

    DashboardMetricsVO vo = service.getDashboardMetrics(1, "7d");

    assertNotNull(vo);
    assertNotNull(vo.getChartData());
    assertEquals(7, vo.getChartData().getLabels().size()); // 验证是否生成了 7 天的 Mock 数据
    assertNotNull(vo.getTodayOverview());
  }

  // ==========================================
  // 2. 桌位列表测试
  // ==========================================
  @Test
  @DisplayName("测试：获取店员端桌位列表")
  void getStaffTables_Success() {
    Tables t = new Tables();
    t.setTableId(2);
    t.setTableNo("A01");
    lenient().when(tablesMapper.selectByExample(any())).thenReturn(Collections.singletonList(t));

    TableStatus ts = new TableStatus();
    ts.setTableId(2);
    ts.setStatus("OCCUPIED");
    ts.setCurrentReservationId(5L);
    lenient().when(tableStatusMapper.selectByExample(any())).thenReturn(Collections.singletonList(ts));

    Reservations r = new Reservations();
    r.setReservationId(5L);
    r.setPartySize(2);
    r.setReservationTime(new Date());
    r.setDurationMin(120);
    lenient().when(reservationsMapper.selectByStoreIdAndStatuses(any(), anyList()))
      .thenReturn(Collections.singletonList(r));

    List<Map<String, Object>> result = service.getStaffTables(1);
    assertFalse(result.isEmpty());
    assertEquals("occupied", result.get(0).get("status"));
  }

  // ==========================================
  // 3. 告警列表测试
  // ==========================================
  @Test
  @DisplayName("测试：获取告警列表")
  void getAlerts_Success() {
    ShiftExceptions ex = new ShiftExceptions();
    ex.setExceptionId(1L);
    ex.setType("OVERSTAY");
    ex.setStatus("PENDING");
    ex.setCreatedAt(new Date());
    lenient().when(shiftExceptionsMapper.selectByExample(any())).thenReturn(Collections.singletonList(ex));

    List<Map<String, Object>> result = service.getAlerts(1);
    assertFalse(result.isEmpty());
    assertEquals("high", result.get(0).get("level")); // 验证 resolveLevel 逻辑
  }

  // ==========================================
  // 4. 接单测试
  // ==========================================
  @Test
  @DisplayName("测试：店员接单 (BOOKED -> CONFIRMED)")
  void acceptOrder_Success() {
    Reservations r = new Reservations();
    r.setReservationId(5L);
    r.setStoreId(1);
    r.setTableId(2);
    r.setStatus("BOOKED");
    lenient().when(reservationsMapper.selectByPrimaryKey(5L)).thenReturn(r);

    TableStatus ts = new TableStatus();
    ts.setTableId(2);
    lenient().when(tableStatusMapper.selectByPrimaryKey(2)).thenReturn(ts);

    lenient().when(reservationsMapper.updateByPrimaryKeySelective(any())).thenReturn(1);
    lenient().doNothing().when(notificationService).createNotification(anyInt(), any(), anyString(), anyString(), anyString(), anyString(), anyString(), anyLong());

    Map<String, Object> result = service.acceptOrder(5L);
    assertTrue((Boolean) result.get("success"));
    assertEquals("CONFIRMED", result.get("status"));
  }

  // ==========================================
  // 5. 订单推进测试
  // ==========================================
  @Test
  @DisplayName("测试：推进订单状态 (SERVING -> COMPLETED)")
  void progressOrder_Success() {
    Reservations r = new Reservations();
    r.setReservationId(5L);
    r.setStoreId(1);
    r.setTableId(2);
    r.setStatus("SERVING"); // 当前状态
    lenient().when(reservationsMapper.selectByPrimaryKey(5L)).thenReturn(r);

    TableStatus ts = new TableStatus();
    ts.setTableId(2);
    lenient().when(tableStatusMapper.selectByPrimaryKey(2)).thenReturn(ts);

    Map<String, Object> result = service.progressOrder(5L, "COMPLETED");
    assertTrue((Boolean) result.get("success"));
    assertEquals("completed", result.get("status"));
  }

  // ==========================================
  // 6. 手动变更桌位状态
  // ==========================================
  @Test
  @DisplayName("测试：变更桌位状态为 IDLE，释放预约")
  void dispatchTable_Success() {
    Tables t = new Tables();
    t.setTableId(2);
    lenient().when(tablesMapper.selectByPrimaryKey(2)).thenReturn(t);

    TableStatus ts = new TableStatus();
    ts.setTableId(2);
    lenient().when(tableStatusMapper.selectByPrimaryKey(2)).thenReturn(ts);

    Map<String, Object> result = service.dispatchTable(2, "IDLE");
    assertTrue((Boolean) result.get("success"));
    assertEquals("IDLE", result.get("status"));
  }

  // ==========================================
  // 7. 退款审核测试 (同意)
  // ==========================================
  @Test
  @DisplayName("测试：退款审核 - 同意 (Approve)")
  void reviewRefund_Approve_Success() {
    RefundRecords refund = new RefundRecords();
    refund.setRefundId(100L);
    refund.setReservationId(5L);
    refund.setStatus("REQUEST_CANCEL"); // 允许审核的状态
    refund.setRefundAmount(new BigDecimal("50"));
    lenient().when(refundRecordsMapper.selectByPrimaryKey(100L)).thenReturn(refund);

    Reservations r = new Reservations();
    r.setReservationId(5L);
    r.setUserId(9L);
    r.setTableId(2);
    r.setPointsEarned(50);
    r.setPointsUsed(10);
    lenient().when(reservationsMapper.selectByPrimaryKey(5L)).thenReturn(r);

    MemberExt me = new MemberExt();
    me.setTotalPoints(100);
    me.setCumulativeAmount(new BigDecimal("500"));
    lenient().when(memberExtMapper.selectByPrimaryKey(9L)).thenReturn(me);

    TableStatus ts = new TableStatus();
    ts.setTableId(2);
    lenient().when(tableStatusMapper.selectByPrimaryKey(2)).thenReturn(ts);

    Map<String, Object> result = service.reviewRefund(100L, "approve", 888L, null);
    assertTrue((Boolean) result.get("success"));
  }

  // ==========================================
  // 8. 退款审核测试 (拒绝)
  // ==========================================
  @Test
  @DisplayName("测试：退款审核 - 拒绝 (Reject)")
  void reviewRefund_Reject_Success() {
    RefundRecords refund = new RefundRecords();
    refund.setRefundId(100L);
    refund.setReservationId(5L);
    refund.setStatus("REQUEST_REFUND"); // 允许审核的状态
    lenient().when(refundRecordsMapper.selectByPrimaryKey(100L)).thenReturn(refund);

    Reservations r = new Reservations();
    r.setReservationId(5L);
    r.setStatus("REFUNDING");
    lenient().when(reservationsMapper.selectByPrimaryKey(5L)).thenReturn(r);

    Map<String, Object> result = service.reviewRefund(100L, "reject", 888L, "不符合退款条件");
    assertTrue((Boolean) result.get("success"));
    assertEquals("REJECTED", result.get("status"));
  }

  // ==========================================
  // 9. 处理告警测试
  // ==========================================
  @Test
  @DisplayName("测试：标记告警已知晓")
  void acknowledgeAlert_Success() {
    ShiftExceptions ex = new ShiftExceptions();
    ex.setExceptionId(1L);
    ex.setStatus("PENDING");
    lenient().when(shiftExceptionsMapper.selectByPrimaryKey(1L)).thenReturn(ex);

    Map<String, Object> result = service.acknowledgeAlert(1L, 888L);
    assertTrue((Boolean) result.get("success"));
  }

  @Test
  @DisplayName("测试：解决告警")
  void resolveAlert_Success() {
    ShiftExceptions ex = new ShiftExceptions();
    ex.setExceptionId(1L);
    ex.setStatus("ACKNOWLEDGED");
    ex.setReason("原原因");
    lenient().when(shiftExceptionsMapper.selectByPrimaryKey(1L)).thenReturn(ex);

    Map<String, Object> result = service.resolveAlert(1L, "已安排人手", 888L);
    assertTrue((Boolean) result.get("success"));
  }

  // ==========================================
  // 10. 订单列表
  // ==========================================
  @Test
  @DisplayName("测试：获取店员端订单")
  void getStaffOrders_Success() {
    Reservations r = new Reservations();
    r.setReservationId(5L);
    r.setStatus("REFUNDING");
    lenient().when(reservationsMapper.selectByStoreId(1)).thenReturn(Collections.singletonList(r));

    RefundRecords rr = new RefundRecords();
    rr.setStatus("COMPLETED");
    lenient().when(refundRecordsMapper.selectByExample(any())).thenReturn(Collections.singletonList(rr));

    List<Map<String, Object>> result = service.getStaffOrders(1);
    assertFalse(result.isEmpty());
    assertEquals("refunded", result.get(0).get("refundStatus"));
  }
  @Test
  @DisplayName("测试：获取退款列表 (覆盖0%方法)")
  void getRefundList_Success() {
    RefundRecords rr = new RefundRecords(); rr.setReservationId(1L);
    when(refundRecordsMapper.selectByExample(any())).thenReturn(Collections.singletonList(rr));

    Reservations r = new Reservations(); r.setStoreId(1);
    when(reservationsMapper.selectByPrimaryKey(1L)).thenReturn(r);

    List<Map<String, Object>> result = service.getRefundList(1);
    assertNotNull(result);
  }

  @Test
  @DisplayName("测试：自动告警扫描 (触发定时器分支)")
  void checkOverstayAlerts_Success() {
    TableStatus ts = new TableStatus(); ts.setStatus("OCCUPIED"); ts.setCurrentReservationId(1L);
    when(tableStatusMapper.selectByExample(any())).thenReturn(Collections.singletonList(ts));

    Reservations r = new Reservations();
    // 设置一个很久之前的预约，保证会触发超时告警逻辑
    r.setReservationTime(new Date(System.currentTimeMillis() - 1000000000L));
    r.setDurationMin(10);
    when(reservationsMapper.selectByPrimaryKey(1L)).thenReturn(r);

    service.checkOverstayAlerts();
    verify(shiftExceptionsMapper, atLeastOnce()).insertSelective(any());
  }
}
