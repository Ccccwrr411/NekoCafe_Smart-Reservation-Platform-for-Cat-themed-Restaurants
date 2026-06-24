package cn.edu.bjfu.nekocafe.service.impl;

import cn.edu.bjfu.nekocafe.dto.ReservationCreateDTO;
import cn.edu.bjfu.nekocafe.dto.RescheduleDTO;
import cn.edu.bjfu.nekocafe.entity.*;
import cn.edu.bjfu.nekocafe.mapper.*;
import cn.edu.bjfu.nekocafe.vo.CurrentReservationVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import cn.edu.bjfu.nekocafe.exception.BusinessException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

  @Mock private ReservationsMapper reservationsMapper;
  @Mock private StoresMapper storesMapper;
  @Mock private TablesMapper tablesMapper;
  @Mock private TableStatusMapper tableStatusMapper;
  @Mock private RefundRecordsMapper refundRecordsMapper;

  @InjectMocks
  private ReservationServiceImpl service;

  // 辅助方法：生成一个完美的假预约记录
  private Reservations createMockReservation(String status) {
    Reservations r = new Reservations();
    r.setReservationId(5L);
    r.setUserId(9L);
    r.setStoreId(1);
    r.setTableId(2);
    r.setStatus(status);
    r.setReservationTime(new Date(System.currentTimeMillis() + 86400000L)); // 明天
    r.setDurationMin(120);
    r.setPartySize(2);
    r.setTotalAmount(java.math.BigDecimal.ZERO);
    r.setOrderAmount(java.math.BigDecimal.ZERO);
    return r;
  }

  @Test
  @DisplayName("测试：创建纯预约 - 成功流程")
  void createReservation_Success() {
    ReservationCreateDTO dto = new ReservationCreateDTO();
    dto.setStoreId(1);
    dto.setTableId(2);
    dto.setReserveDate("2026-06-20");
    dto.setReserveTime("14:00");
    dto.setDuration(2);
    dto.setPersons(2);

    // 1. 模拟时段冲突检查：返回空列表，代表没有冲突
    lenient().when(reservationsMapper.selectByTableIdAndStatuses(anyInt(), anyList()))
      .thenReturn(Collections.emptyList());

    // 2. 模拟插入数据库成功并返回 ID
    lenient().when(reservationsMapper.insertSelective(any(Reservations.class))).thenAnswer(invocation -> {
      Reservations r = invocation.getArgument(0);
      r.setReservationId(999L);
      return 1;
    });

    // 3. 模拟乐观锁更新桌位状态
    TableStatus ts = new TableStatus();
    ts.setVersion(1);
    lenient().when(tableStatusMapper.selectByPrimaryKey(2)).thenReturn(ts);
    lenient().when(tableStatusMapper.reserveTableOptimistic(anyInt(), anyLong(), anyInt())).thenReturn(1);

    // 执行测试
    Map<String, Object> result = service.createReservation(9L, dto);

    assertNotNull(result);
    assertEquals("BOOKED", result.get("status"));
    assertTrue(result.get("orderId").toString().startsWith("ORD"));
    verify(reservationsMapper, times(1)).insertSelective(any());
  }

  @Test
  @DisplayName("测试：改约 (实际逻辑为取消旧订单) - 成功流程")
  void reschedule_Success() {
    RescheduleDTO dto = new RescheduleDTO();
    dto.setOrderId("ORD0000000005");

    // 1. 模拟查到符合条件的预约单 (userId=9L, status=BOOKED)
    when(reservationsMapper.selectByPrimaryKey(5L)).thenReturn(createMockReservation("BOOKED"));

    // 2. 模拟更新状态成功
    lenient().when(reservationsMapper.updateByPrimaryKeySelective(any())).thenReturn(1);

    // 3. 模拟释放桌位逻辑
    lenient().when(reservationsMapper.countActiveByTableIdExcluding(anyInt(), anyList(), anyLong())).thenReturn(0);
    TableStatus ts = new TableStatus();
    ts.setVersion(1);
    lenient().when(tableStatusMapper.selectByPrimaryKey(2)).thenReturn(ts);
    lenient().when(tableStatusMapper.releaseTableOptimistic(anyInt(), anyLong(), anyInt())).thenReturn(1);

    // 执行测试
    Map<String, Object> result = service.reschedule(9L, dto);

    assertNotNull(result);
    assertEquals("CANCEL_BOOKING", result.get("status"));
    assertEquals("ORD0000000005", result.get("orderId"));
  }

  @Test
  @DisplayName("测试：获取当前活跃预约列表")
  void getCurrentReservations_Success() {
    Reservations mockRes = createMockReservation("BOOKED");
    when(reservationsMapper.selectByUserIdAndStatuses(eq(9L), anyList()))
      .thenReturn(Collections.singletonList(mockRes));

    // 模拟关联查门店
    Stores store = new Stores();
    store.setName("猫咖(五道口店)");
    lenient().when(storesMapper.selectByPrimaryKey(1)).thenReturn(store);

    // 模拟关联查桌位
    Tables table = new Tables();
    table.setTableNo("A01");
    table.setTableType("窗边座");
    table.setCapacity(2);
    table.setCatTheme("布偶猫");
    lenient().when(tablesMapper.selectByPrimaryKey(2)).thenReturn(table);

    List<CurrentReservationVO> result = service.getCurrentReservations(9L);

    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals("猫咖(五道口店)", result.get(0).getStoreName());
    assertEquals("A01", result.get(0).getTableName());
  }

  @Test
  @DisplayName("测试：重新激活已取消订单")
  void reactivateReservation_Success() {
    // 核心校验：只有 CANCEL_ORDER 状态可以激活
    Reservations mockRes = createMockReservation("CANCEL_ORDER");
    when(reservationsMapper.selectByPrimaryKey(5L)).thenReturn(mockRes);

    lenient().when(reservationsMapper.updateByPrimaryKeySelective(any())).thenReturn(1);

    Stores store = new Stores();
    store.setName("猫咖(五道口店)");
    lenient().when(storesMapper.selectByPrimaryKey(1)).thenReturn(store);

    Tables table = new Tables();
    table.setTableNo("A01");
    lenient().when(tablesMapper.selectByPrimaryKey(2)).thenReturn(table);

    Map<String, Object> result = service.reactivateReservation(9L, "ORD0000000005");

    assertNotNull(result);
    assertEquals("BOOKED", result.get("status"));
    assertEquals("猫咖(五道口店)", result.get("storeName"));
  }
  @Test
  @DisplayName("测试：createReservation - 触发参数校验拦截")
  void createReservation_BadArgs() {
    // 测试：DTO 为空抛出 BusinessException
    ReservationCreateDTO dto = new ReservationCreateDTO();
    // 不设 storeId 和 tableId，触发 if (dto.getStoreId() == null || ...)
    assertThrows(BusinessException.class, () -> service.createReservation(9L, dto));
  }

  @Test
  @DisplayName("测试：reschedule - 覆盖权限与状态校验")
  void reschedule_ValidationFailures() {
    // 1. 测试：订单不存在
    RescheduleDTO dto = new RescheduleDTO(); dto.setOrderId("ORD9999999999");
    when(reservationsMapper.selectByPrimaryKey(anyLong())).thenReturn(null);
    assertThrows(BusinessException.class, () -> service.reschedule(9L, dto));

    // 2. 测试：订单状态不允许改约 (比如已经是 COMPLETED)
    Reservations r = createMockReservation("COMPLETED");
    when(reservationsMapper.selectByPrimaryKey(5L)).thenReturn(r);
    dto.setOrderId("ORD0000000005");
    assertThrows(BusinessException.class, () -> service.reschedule(9L, dto));
  }

  @Test
  @DisplayName("测试：reactivateReservation - 覆盖状态流转拦截")
  void reactivateReservation_InvalidStatus() {
    Reservations r = createMockReservation("BOOKED"); // 状态不是 CANCEL_ORDER
    when(reservationsMapper.selectByPrimaryKey(5L)).thenReturn(r);

    // 触发：当前订单状态不允许重新激活
    assertThrows(BusinessException.class, () -> service.reactivateReservation(9L, "ORD0000000005"));
  }
}
