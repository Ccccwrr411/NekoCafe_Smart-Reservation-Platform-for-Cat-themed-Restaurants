package cn.edu.bjfu.nekocafe.service.impl;

import cn.edu.bjfu.nekocafe.entity.Reservations;
import cn.edu.bjfu.nekocafe.entity.StoreDailyStats;
import cn.edu.bjfu.nekocafe.mapper.MemberExtMapper;
import cn.edu.bjfu.nekocafe.mapper.ReservationsMapper;
import cn.edu.bjfu.nekocafe.mapper.StoreDailyStatsMapper;
import cn.edu.bjfu.nekocafe.mapper.UsersMapper;
import cn.edu.bjfu.nekocafe.vo.DashboardMetricsVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DashboardServiceImplTest {
  private DashboardServiceImpl service;
  private StoreDailyStatsMapper storeDailyStatsMapper;
  private ReservationsMapper reservationsMapper;
  private UsersMapper usersMapper;

  @BeforeEach
  void setUp() {
    service = new DashboardServiceImpl();
    storeDailyStatsMapper = mock(StoreDailyStatsMapper.class);
    reservationsMapper = mock(ReservationsMapper.class);
    usersMapper = mock(UsersMapper.class);
    ReflectionTestUtils.setField(service, "storeDailyStatsMapper", storeDailyStatsMapper);
    ReflectionTestUtils.setField(service, "reservationsMapper", reservationsMapper);
    ReflectionTestUtils.setField(service, "usersMapper", usersMapper);
    ReflectionTestUtils.setField(service, "memberExtMapper", mock(MemberExtMapper.class));
  }

  @Test
  void getMetrics() {
    StoreDailyStats stats = new StoreDailyStats();
    stats.setStatDate(new Date());
    stats.setRevenuePerSeat(new BigDecimal("120.5"));
    stats.setTableTurnoverRate(new BigDecimal("2.5"));
    stats.setTotalReservations(10);
    stats.setRepeatCustomers(3);
    when(storeDailyStatsMapper.selectByExample(any())).thenReturn(List.of(stats));
    Reservations reservation = new Reservations();
    reservation.setTotalAmount(new BigDecimal("88"));
    when(reservationsMapper.selectByExample(any())).thenReturn(List.of(reservation));
    when(usersMapper.countByExample(any())).thenReturn(2L);

    DashboardMetricsVO result = service.getMetrics(1, "7d");

    assertEquals(1, result.getStoreId());
    assertEquals(88, result.getTodayOverview().getRevenue());
    assertEquals(2, result.getTodayOverview().getNewMembers());
    assertEquals(1, result.getChartData().getLabels().size());
  }
}
