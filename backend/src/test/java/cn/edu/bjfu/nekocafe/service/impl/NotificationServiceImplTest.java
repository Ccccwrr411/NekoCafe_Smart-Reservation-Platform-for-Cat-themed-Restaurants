package cn.edu.bjfu.nekocafe.service.impl;

import cn.edu.bjfu.nekocafe.entity.Notifications;
import cn.edu.bjfu.nekocafe.mapper.NotificationsMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class NotificationServiceImplTest {
  private NotificationServiceImpl service(NotificationsMapper mapper) {
    NotificationServiceImpl service = new NotificationServiceImpl();
    ReflectionTestUtils.setField(service, "notificationsMapper", mapper);
    return service;
  }

  private Notifications notification() {
    Notifications n = new Notifications();
    n.setNotificationId(1L);
    n.setStoreId(1);
    n.setUserId(9L);
    n.setTitle("提醒");
    n.setContent("内容");
    n.setIsRead(false);
    n.setCreatedAt(new Date());
    return n;
  }

  @Test
  void getStoreNotifications() {
    NotificationsMapper mapper = mock(NotificationsMapper.class);
    when(mapper.selectByStore(1, 10, 10)).thenReturn(List.of(notification()));

    List<Map<String, Object>> result = service(mapper).getStoreNotifications(1, 2, 10);

    assertEquals(1, result.size());
    assertEquals(false, result.get(0).get("isRead"));
  }

  @Test
  void getUserNotifications() {
    NotificationsMapper mapper = mock(NotificationsMapper.class);
    when(mapper.selectByUser(9L, 30, 0)).thenReturn(List.of(notification()));

    List<Map<String, Object>> result = service(mapper).getUserNotifications(9L, null, null);

    assertEquals(1, result.size());
  }

  @Test
  void getStoreUnreadCount() {
    NotificationsMapper mapper = mock(NotificationsMapper.class);
    when(mapper.countUnreadByStore(1)).thenReturn(3);

    assertEquals(3, service(mapper).getStoreUnreadCount(1));
  }

  @Test
  void getUserUnreadCount() {
    NotificationsMapper mapper = mock(NotificationsMapper.class);
    when(mapper.countUnreadByUser(9L)).thenReturn(2);

    assertEquals(2, service(mapper).getUserUnreadCount(9L));
  }

  @Test
  void markAsRead() {
    NotificationsMapper mapper = mock(NotificationsMapper.class);
    when(mapper.markAsRead(1L)).thenReturn(1);

    assertEquals(true, service(mapper).markAsRead(1L).get("success"));
  }

  @Test
  void markAllStoreRead() {
    NotificationsMapper mapper = mock(NotificationsMapper.class);
    when(mapper.markAllAsReadByStore(1)).thenReturn(4);

    Map<String, Object> result = service(mapper).markAllStoreRead(1);

    assertEquals(true, result.get("success"));
    assertEquals(4, result.get("count"));
  }

  @Test
  void markAllUserRead() {
    NotificationsMapper mapper = mock(NotificationsMapper.class);
    when(mapper.markAllAsReadByUser(9L)).thenReturn(5);

    assertEquals(5, service(mapper).markAllUserRead(9L).get("count"));
  }

  @Test
  void createNotification() {
    NotificationsMapper mapper = mock(NotificationsMapper.class);

    service(mapper).createNotification(1, 9L, "USER", "INFO", "标题", "内容", "ORDER", 7L);

    verify(mapper).insertSelective(argThat(n -> !n.getIsRead() && n.getUserId().equals(9L)));
  }
}
