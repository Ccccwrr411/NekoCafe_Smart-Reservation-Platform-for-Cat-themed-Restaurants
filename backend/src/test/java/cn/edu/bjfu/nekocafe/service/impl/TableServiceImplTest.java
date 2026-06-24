package cn.edu.bjfu.nekocafe.service.impl;

import cn.edu.bjfu.nekocafe.entity.Reservations;
import cn.edu.bjfu.nekocafe.entity.Tables;
import cn.edu.bjfu.nekocafe.mapper.ReservationsMapper;
import cn.edu.bjfu.nekocafe.mapper.TableStatusMapper;
import cn.edu.bjfu.nekocafe.mapper.TablesMapper;
import cn.edu.bjfu.nekocafe.vo.TableVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Calendar;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TableServiceImplTest {
  private TableServiceImpl service;
  private TablesMapper tablesMapper;
  private ReservationsMapper reservationsMapper;

  @BeforeEach
  void setUp() {
    service = new TableServiceImpl();
    tablesMapper = mock(TablesMapper.class);
    reservationsMapper = mock(ReservationsMapper.class);
    ReflectionTestUtils.setField(service, "tablesMapper", tablesMapper);
    ReflectionTestUtils.setField(service, "tableStatusMapper", mock(TableStatusMapper.class));
    ReflectionTestUtils.setField(service, "reservationsMapper", reservationsMapper);
  }

  @Test
  void listTables() {
    Tables table = new Tables();
    table.setTableId(10);
    table.setTableNo("A01");
    table.setTableType("包间");
    table.setCapacity(4);
    table.setCatTheme("布偶");
    when(tablesMapper.selectByExample(any())).thenReturn(List.of(table));

    Reservations existing = new Reservations();
    existing.setTableId(10);
    Calendar cal = Calendar.getInstance();
    cal.set(2026, Calendar.JUNE, 16, 10, 30, 0);
    existing.setReservationTime(cal.getTime());
    existing.setDurationMin(120);
    when(reservationsMapper.selectByStoreIdAndStatuses(eq(1), anyList())).thenReturn(List.of(existing));

    List<TableVO> tables = service.listTables(1, "2026-06-16", "10:00", 2);

    assertEquals(1, tables.size());
    assertEquals("booked", tables.get(0).getStatus());
    assertEquals(50, tables.get(0).getPrice());
  }
}
