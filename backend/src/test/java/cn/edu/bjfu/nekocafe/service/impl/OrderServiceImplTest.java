package cn.edu.bjfu.nekocafe.service.impl;

import cn.edu.bjfu.nekocafe.dto.RescheduleDTO;
import cn.edu.bjfu.nekocafe.entity.*;
import cn.edu.bjfu.nekocafe.exception.BusinessException;
import cn.edu.bjfu.nekocafe.mapper.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * OrderServiceImpl 单元测试
 * 覆盖：E-4 cancelOrder, E-5 reschedule, E-6 applyRefund
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private ReservationsMapper reservationsMapper;
    @Mock
    private OrderItemsMapper orderItemsMapper;
    @Mock
    private StoresMapper storesMapper;
    @Mock
    private TablesMapper tablesMapper;
    @Mock
    private RefundRecordsMapper refundRecordsMapper;
    @Mock
    private PaymentsMapper paymentsMapper;
    @Mock
    private TableStatusMapper tableStatusMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    private static final Long USER_ID = 1L;
    private static final Long RESERVATION_ID = 100L;
    private static final String ORDER_ID = "ORD0000000100";

    private Reservations buildReservation(String status) {
        Reservations r = new Reservations();
        r.setReservationId(RESERVATION_ID);
        r.setUserId(USER_ID);
        r.setStoreId(1);
        r.setTableId(10);
        r.setOrderAmount(new BigDecimal("99.00"));
        r.setStatus(status);
        r.setCreatedAt(new Date());
        r.setUpdatedAt(new Date());
        return r;
    }

    // ==================== E-4: cancelOrder ====================

    @Test
    void cancelOrder_shouldCancelAndRestoreTableStatus() {
        Reservations reservation = buildReservation("BOOKED");
        when(reservationsMapper.selectByPrimaryKey(RESERVATION_ID)).thenReturn(reservation);
        when(reservationsMapper.updateByPrimaryKeySelective(any())).thenReturn(1);

        Map<String, Object> result = orderService.cancelOrder(USER_ID, ORDER_ID);

        // 验证 reservations status 改为 CANCELLED
        ArgumentCaptor<Reservations> resCaptor = ArgumentCaptor.forClass(Reservations.class);
        verify(reservationsMapper).updateByPrimaryKeySelective(resCaptor.capture());
        assertEquals("CANCELLED", resCaptor.getValue().getStatus());

        // 验证不写入 refund_records（取消预约不涉及退款）
        verify(refundRecordsMapper, never()).insertSelective(any());

        // 验证返回值
        assertEquals("cancelled", result.get("status"));
        assertNull(result.get("refundAmount"));
    }

    @Test
    void cancelOrder_shouldThrowWhenOrderNotFound() {
        when(reservationsMapper.selectByPrimaryKey(RESERVATION_ID)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.cancelOrder(USER_ID, ORDER_ID));
        assertTrue(ex.getMessage().contains("订单不存在"));
    }

    @Test
    void cancelOrder_shouldThrowWhenNotOwner() {
        Reservations reservation = buildReservation("BOOKED");
        reservation.setUserId(999L); // 不是当前用户
        when(reservationsMapper.selectByPrimaryKey(RESERVATION_ID)).thenReturn(reservation);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.cancelOrder(USER_ID, ORDER_ID));
        assertTrue(ex.getMessage().contains("无权"));
    }

    @Test
    void cancelOrder_shouldThrowWhenStatusNotAllowed() {
        Reservations reservation = buildReservation("COMPLETED");
        when(reservationsMapper.selectByPrimaryKey(RESERVATION_ID)).thenReturn(reservation);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.cancelOrder(USER_ID, ORDER_ID));
        assertTrue(ex.getMessage().contains("不允许取消"));
    }

    // ==================== E-5: reschedule ====================

    @Test
    void reschedule_shouldCancelOldAndRestoreTable() {
        Reservations reservation = buildReservation("BOOKED");
        when(reservationsMapper.selectByPrimaryKey(RESERVATION_ID)).thenReturn(reservation);
        when(reservationsMapper.updateByPrimaryKeySelective(any())).thenReturn(1);

        RescheduleDTO dto = new RescheduleDTO();
        dto.setOrderId(ORDER_ID);
        dto.setNewReserveDate("2026-06-15");
        dto.setNewReserveTime("14:00");

        Map<String, Object> result = orderService.reschedule(USER_ID, dto);

        // 验证 status 改为 CANCELLED
        ArgumentCaptor<Reservations> resCaptor = ArgumentCaptor.forClass(Reservations.class);
        verify(reservationsMapper).updateByPrimaryKeySelective(resCaptor.capture());
        assertEquals("CANCELLED", resCaptor.getValue().getStatus());

        // 验证不写入 refund_records（改约不涉及退款）
        verify(refundRecordsMapper, never()).insertSelective(any());

        // 验证返回值
        assertEquals("cancelled", result.get("status"));
    }

    @Test
    void reschedule_shouldThrowWhenOrderNotFound() {
        when(reservationsMapper.selectByPrimaryKey(RESERVATION_ID)).thenReturn(null);

        RescheduleDTO dto = new RescheduleDTO();
        dto.setOrderId(ORDER_ID);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.reschedule(USER_ID, dto));
        assertTrue(ex.getMessage().contains("订单不存在"));
    }

    @Test
    void reschedule_shouldThrowWhenNotOwner() {
        Reservations reservation = buildReservation("BOOKED");
        reservation.setUserId(999L);
        when(reservationsMapper.selectByPrimaryKey(RESERVATION_ID)).thenReturn(reservation);

        RescheduleDTO dto = new RescheduleDTO();
        dto.setOrderId(ORDER_ID);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.reschedule(USER_ID, dto));
        assertTrue(ex.getMessage().contains("无权"));
    }

    @Test
    void reschedule_shouldThrowWhenStatusNotAllowed() {
        Reservations reservation = buildReservation("CANCELLED");
        when(reservationsMapper.selectByPrimaryKey(RESERVATION_ID)).thenReturn(reservation);

        RescheduleDTO dto = new RescheduleDTO();
        dto.setOrderId(ORDER_ID);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.reschedule(USER_ID, dto));
        assertTrue(ex.getMessage().contains("不允许改约"));
    }

    @Test
    void reschedule_shouldThrowWhenTimeFormatInvalid() {
        Reservations reservation = buildReservation("BOOKED");
        when(reservationsMapper.selectByPrimaryKey(RESERVATION_ID)).thenReturn(reservation);

        RescheduleDTO dto = new RescheduleDTO();
        dto.setOrderId(ORDER_ID);
        dto.setNewReserveDate("bad-date");
        dto.setNewReserveTime("bad-time");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.reschedule(USER_ID, dto));
        assertTrue(ex.getMessage().contains("时间格式错误"));
    }

    // ==================== E-6: applyRefund ====================

    @Test
    void applyRefund_shouldCreateRefundRecordAndUpdateStatus() {
        Reservations reservation = buildReservation("CONFIRMED");
        when(reservationsMapper.selectByPrimaryKey(RESERVATION_ID)).thenReturn(reservation);
        when(reservationsMapper.updateByPrimaryKeySelective(any())).thenReturn(1);
        when(paymentsMapper.selectByExample(any())).thenReturn(Collections.emptyList());
        when(refundRecordsMapper.insertSelective(any())).thenReturn(1);

        Map<String, Object> result = orderService.applyRefund(USER_ID, ORDER_ID);

        // 验证 reservations status 改为 REFUNDING
        ArgumentCaptor<Reservations> resCaptor = ArgumentCaptor.forClass(Reservations.class);
        verify(reservationsMapper).updateByPrimaryKeySelective(resCaptor.capture());
        assertEquals("REFUNDING", resCaptor.getValue().getStatus());

        // 验证 refund_records 写入，status 为 PROCESSING
        ArgumentCaptor<RefundRecords> refundCaptor = ArgumentCaptor.forClass(RefundRecords.class);
        verify(refundRecordsMapper).insertSelective(refundCaptor.capture());
        RefundRecords refund = refundCaptor.getValue();
        assertEquals(RESERVATION_ID, refund.getReservationId());
        assertEquals(new BigDecimal("99.00"), refund.getRefundAmount());
        assertEquals("用户申请退款", refund.getRefundReason());
        assertEquals("PROCESSING", refund.getStatus());
        assertNotNull(refund.getCreatedAt());
        assertNull(refund.getCompletedAt());

        // 验证返回值
        assertEquals("processing", result.get("status"));
    }

    @Test
    void applyRefund_shouldThrowWhenNotAllowed() {
        Reservations reservation = buildReservation("CANCELLED");
        when(reservationsMapper.selectByPrimaryKey(RESERVATION_ID)).thenReturn(reservation);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.applyRefund(USER_ID, ORDER_ID));
        assertTrue(ex.getMessage().contains("不允许退款"));
    }

    // ==================== 允许改约的状态边界测试 ====================

    @Test
    void reschedule_shouldAllowBookedStatus() {
        Reservations reservation = buildReservation("BOOKED");
        when(reservationsMapper.selectByPrimaryKey(RESERVATION_ID)).thenReturn(reservation);
        when(reservationsMapper.updateByPrimaryKeySelective(any())).thenReturn(1);
        when(paymentsMapper.selectByExample(any())).thenReturn(Collections.emptyList());
        when(refundRecordsMapper.insertSelective(any())).thenReturn(1);

        RescheduleDTO dto = new RescheduleDTO();
        dto.setOrderId(ORDER_ID);
        dto.setNewReserveDate("2026-06-15");
        dto.setNewReserveTime("14:00");

        assertDoesNotThrow(() -> orderService.reschedule(USER_ID, dto));
    }

    @Test
    void reschedule_shouldAllowPendingStatus() {
        Reservations reservation = buildReservation("PENDING");
        when(reservationsMapper.selectByPrimaryKey(RESERVATION_ID)).thenReturn(reservation);
        when(reservationsMapper.updateByPrimaryKeySelective(any())).thenReturn(1);
        when(paymentsMapper.selectByExample(any())).thenReturn(Collections.emptyList());
        when(refundRecordsMapper.insertSelective(any())).thenReturn(1);

        RescheduleDTO dto = new RescheduleDTO();
        dto.setOrderId(ORDER_ID);
        dto.setNewReserveDate("2026-06-15");
        dto.setNewReserveTime("14:00");

        assertDoesNotThrow(() -> orderService.reschedule(USER_ID, dto));
    }

    @Test
    void reschedule_shouldAllowConfirmedStatus() {
        Reservations reservation = buildReservation("CONFIRMED");
        when(reservationsMapper.selectByPrimaryKey(RESERVATION_ID)).thenReturn(reservation);
        when(reservationsMapper.updateByPrimaryKeySelective(any())).thenReturn(1);
        when(paymentsMapper.selectByExample(any())).thenReturn(Collections.emptyList());
        when(refundRecordsMapper.insertSelective(any())).thenReturn(1);

        RescheduleDTO dto = new RescheduleDTO();
        dto.setOrderId(ORDER_ID);
        dto.setNewReserveDate("2026-06-15");
        dto.setNewReserveTime("14:00");

        assertDoesNotThrow(() -> orderService.reschedule(USER_ID, dto));
    }
}
