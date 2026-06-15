package cn.edu.bjfu.nekocafe.service.impl;

import cn.edu.bjfu.nekocafe.dto.QueueTakeDTO;
import cn.edu.bjfu.nekocafe.entity.Users;
import cn.edu.bjfu.nekocafe.mapper.QueueMapper;
import cn.edu.bjfu.nekocafe.mapper.UsersMapper;
import cn.edu.bjfu.nekocafe.vo.QueueStatusVO;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class QueueServiceImplTest {
  private QueueServiceImpl service(QueueMapper queueMapper, UsersMapper usersMapper,
                                   StringRedisTemplate redisTemplate,
                                   ValueOperations<String, String> valueOperations) {
    QueueServiceImpl service = new QueueServiceImpl();
    ReflectionTestUtils.setField(service, "queueMapper", queueMapper);
    ReflectionTestUtils.setField(service, "usersMapper", usersMapper);
    ReflectionTestUtils.setField(service, "stringRedisTemplate", redisTemplate);
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    return service;
  }

  @Test
  void getQueueStatus() {
    QueueMapper queueMapper = mock(QueueMapper.class);
    UsersMapper usersMapper = mock(UsersMapper.class);
    StringRedisTemplate redis = mock(StringRedisTemplate.class);
    ValueOperations<String, String> ops = mock(ValueOperations.class);
    cn.edu.bjfu.nekocafe.entity.Queue q = new cn.edu.bjfu.nekocafe.entity.Queue();
    q.setQueueId(1L);
    q.setUserId(9L);
    q.setQueueNumber("Q005");
    q.setPartySize(2);
    q.setPreferredTableType("双人座");
    when(queueMapper.selectByExample(any())).thenReturn(List.of(), List.of(q), List.of(), List.of());
    when(ops.get("nekocafe:current:1")).thenReturn("4");
    Users user = new Users();
    user.setNickname("猫友");
    when(usersMapper.selectByPrimaryKey(9L)).thenReturn(user);

    QueueStatusVO result = service(queueMapper, usersMapper, redis, ops).getQueueStatus(1, 9L);

    assertEquals(1, result.getWaitingCount());
    assertEquals(5, result.getMyNumber());
    assertEquals(4, result.getCurrentNumber());
  }

  @Test
  void takeNumber() {
    QueueMapper queueMapper = mock(QueueMapper.class);
    StringRedisTemplate redis = mock(StringRedisTemplate.class);
    ValueOperations<String, String> ops = mock(ValueOperations.class);
    when(queueMapper.selectByExample(any())).thenReturn(List.of());
    when(ops.increment(anyString())).thenReturn(3L);
    when(queueMapper.countByExample(any())).thenReturn(3L);
    QueueTakeDTO dto = new QueueTakeDTO();
    dto.setStoreId(1);
    dto.setPersons(2);
    dto.setType("双人座");

    Map<String, Object> result = service(queueMapper, mock(UsersMapper.class), redis, ops).takeNumber(9L, dto);

    assertEquals(3, result.get("number"));
    assertEquals(2, result.get("ahead"));
    verify(queueMapper).insertSelective(any());
  }

  @Test
  void callNumber() {
    QueueMapper queueMapper = mock(QueueMapper.class);
    StringRedisTemplate redis = mock(StringRedisTemplate.class);
    ValueOperations<String, String> ops = mock(ValueOperations.class);
    cn.edu.bjfu.nekocafe.entity.Queue q = new cn.edu.bjfu.nekocafe.entity.Queue();
    q.setQueueId(1L);
    q.setStoreId(1);
    q.setUserId(9L);
    q.setStatus("WAITING");
    q.setQueueNumber("Q003");
    q.setPartySize(2);
    when(queueMapper.selectByPrimaryKey(1L)).thenReturn(q);

    Map<String, Object> result = service(queueMapper, mock(UsersMapper.class), redis, ops).callNumber(1, 1L);

    assertEquals(3, result.get("number"));
    assertEquals("CALLED", q.getStatus());
    verify(ops).set(eq("nekocafe:current:1"), eq("3"), anyLong(), any());
  }

  @Test
  void confirmNumber() {
    QueueMapper queueMapper = mock(QueueMapper.class);
    cn.edu.bjfu.nekocafe.entity.Queue q = new cn.edu.bjfu.nekocafe.entity.Queue();
    q.setQueueId(1L);
    q.setUserId(9L);
    q.setStatus("CALLED");
    when(queueMapper.selectByPrimaryKey(1L)).thenReturn(q);

    service(queueMapper, mock(UsersMapper.class), mock(StringRedisTemplate.class), mock(ValueOperations.class)).confirmNumber(9L, 1L);

    assertEquals("KNOWN", q.getStatus());
    verify(queueMapper).updateByPrimaryKeySelective(q);
  }
}
