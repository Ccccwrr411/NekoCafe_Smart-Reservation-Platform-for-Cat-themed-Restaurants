package cn.edu.bjfu.nekocafe.service.impl;

import cn.edu.bjfu.nekocafe.entity.ShiftExceptions;
import cn.edu.bjfu.nekocafe.entity.StaffSchedules;
import cn.edu.bjfu.nekocafe.entity.StaffShifts;
import cn.edu.bjfu.nekocafe.entity.Users;
import cn.edu.bjfu.nekocafe.mapper.ShiftExceptionsMapper;
import cn.edu.bjfu.nekocafe.mapper.StaffSchedulesMapper;
import cn.edu.bjfu.nekocafe.mapper.StaffShiftsMapper;
import cn.edu.bjfu.nekocafe.mapper.UsersMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ManagerServiceImplTest {
  private ManagerServiceImpl service(StaffSchedulesMapper schedulesMapper,
                                     StaffShiftsMapper shiftsMapper,
                                     ShiftExceptionsMapper exceptionsMapper,
                                     UsersMapper usersMapper) {
    ManagerServiceImpl service = new ManagerServiceImpl();
    ReflectionTestUtils.setField(service, "staffSchedulesMapper", schedulesMapper);
    ReflectionTestUtils.setField(service, "staffShiftsMapper", shiftsMapper);
    ReflectionTestUtils.setField(service, "shiftExceptionsMapper", exceptionsMapper);
    ReflectionTestUtils.setField(service, "usersMapper", usersMapper);
    return service;
  }

  @Test
  void getSchedules() {
    StaffSchedulesMapper schedulesMapper = mock(StaffSchedulesMapper.class);
    StaffShiftsMapper shiftsMapper = mock(StaffShiftsMapper.class);
    UsersMapper usersMapper = mock(UsersMapper.class);
    StaffSchedules schedule = new StaffSchedules();
    schedule.setScheduleId(1L);
    schedule.setStaffId(9L);
    schedule.setWorkDate(new Date());
    schedule.setShiftId(2);
    when(schedulesMapper.selectByExample(any())).thenReturn(List.of(schedule));
    Users user = new Users();
    user.setRealName("店员甲");
    when(usersMapper.selectByPrimaryKey(9L)).thenReturn(user);
    StaffShifts shift = new StaffShifts();
    shift.setShiftName("早班");
    when(shiftsMapper.selectByPrimaryKey(2)).thenReturn(shift);

    List<Map<String, Object>> result = service(schedulesMapper, shiftsMapper, mock(ShiftExceptionsMapper.class), usersMapper).getSchedules(1);

    assertEquals("店员甲", result.get(0).get("staffName"));
    assertEquals("早班", result.get(0).get("shiftName"));
  }

  @Test
  void getShifts() {
    StaffShiftsMapper shiftsMapper = mock(StaffShiftsMapper.class);
    StaffShifts shift = new StaffShifts();
    shift.setShiftId(2);
    shift.setShiftName("早班");
    when(shiftsMapper.selectByExample(any())).thenReturn(List.of(shift));

    List<Map<String, Object>> result = service(mock(StaffSchedulesMapper.class), shiftsMapper, mock(ShiftExceptionsMapper.class), mock(UsersMapper.class)).getShifts();

    assertEquals(1, result.size());
    assertEquals("早班", result.get(0).get("shiftName"));
  }

  @Test
  void getExceptions() {
    ShiftExceptionsMapper exceptionsMapper = mock(ShiftExceptionsMapper.class);
    ShiftExceptions exception = new ShiftExceptions();
    exception.setExceptionId(3L);
    exception.setStaffId(9L);
    exception.setStatus("PENDING");
    when(exceptionsMapper.selectByExample(any())).thenReturn(List.of(exception));

    List<Map<String, Object>> result = service(mock(StaffSchedulesMapper.class), mock(StaffShiftsMapper.class), exceptionsMapper, mock(UsersMapper.class)).getExceptions(1);

    assertEquals(1, result.size());
    assertEquals("PENDING", result.get(0).get("status"));
  }

  @Test
  void reviewException() {
    ShiftExceptionsMapper exceptionsMapper = mock(ShiftExceptionsMapper.class);
    ShiftExceptions exception = new ShiftExceptions();
    exception.setExceptionId(3L);
    exception.setStatus("PENDING");
    when(exceptionsMapper.selectByPrimaryKey(3L)).thenReturn(exception);

    Map<String, Object> result = service(mock(StaffSchedulesMapper.class), mock(StaffShiftsMapper.class), exceptionsMapper, mock(UsersMapper.class)).reviewException(3L, "approve");

    assertEquals("APPROVED", result.get("status"));
    verify(exceptionsMapper).updateByPrimaryKeySelective(exception);
  }
}
