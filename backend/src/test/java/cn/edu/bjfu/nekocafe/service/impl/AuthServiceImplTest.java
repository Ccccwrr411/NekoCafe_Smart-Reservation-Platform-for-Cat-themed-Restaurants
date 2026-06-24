package cn.edu.bjfu.nekocafe.service.impl;

import cn.edu.bjfu.nekocafe.dto.LoginDTO;
import cn.edu.bjfu.nekocafe.dto.PhoneLoginDTO;
import cn.edu.bjfu.nekocafe.dto.RegisterDTO;
import cn.edu.bjfu.nekocafe.entity.MemberExt;
import cn.edu.bjfu.nekocafe.entity.UserRoles;
import cn.edu.bjfu.nekocafe.entity.Users;
import cn.edu.bjfu.nekocafe.mapper.MemberExtMapper;
import cn.edu.bjfu.nekocafe.mapper.StoresMapper;
import cn.edu.bjfu.nekocafe.mapper.UserRolesMapper;
import cn.edu.bjfu.nekocafe.mapper.UsersMapper;
import cn.edu.bjfu.nekocafe.vo.LoginVO;
import org.mindrot.jbcrypt.BCrypt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthServiceImplTest {
  private AuthServiceImpl service;
  private UsersMapper usersMapper;
  private MemberExtMapper memberExtMapper;
  private UserRolesMapper userRolesMapper;
  private RedisTemplate<String, Object> redisTemplate;
  private ValueOperations<String, Object> valueOperations;

  @BeforeEach
  void setUp() {
    service = new AuthServiceImpl();
    usersMapper = mock(UsersMapper.class);
    memberExtMapper = mock(MemberExtMapper.class);
    userRolesMapper = mock(UserRolesMapper.class);
    redisTemplate = mock(RedisTemplate.class);
    valueOperations = mock(ValueOperations.class);
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    ReflectionTestUtils.setField(service, "usersMapper", usersMapper);
    ReflectionTestUtils.setField(service, "memberExtMapper", memberExtMapper);
    ReflectionTestUtils.setField(service, "userRolesMapper", userRolesMapper);
    ReflectionTestUtils.setField(service, "storesMapper", mock(StoresMapper.class));
    ReflectionTestUtils.setField(service, "redisTemplate", redisTemplate);
  }

  @Test
  void wxQuickLogin() {
    Users user = user();
    when(usersMapper.selectByExample(any())).thenReturn(List.of(user));
    when(memberExtMapper.selectByPrimaryKey(9L)).thenReturn(new MemberExt());
    when(userRolesMapper.selectByExample(any())).thenReturn(List.of(role()));

    LoginVO result = service.wxQuickLogin("openid");

    assertNotNull(result.getToken());
    assertEquals(9L, result.getUserInfo().getId());
  }

  @Test
  void wxLogin() {
    LoginDTO dto = new LoginDTO();
    dto.setPhone("13812345678");
    dto.setSmsCode("123456");
    dto.setCode("wx-code");
    when(valueOperations.get("sms:13812345678")).thenReturn("123456");
    when(usersMapper.selectByExample(any())).thenReturn(List.of(user()));
    when(memberExtMapper.selectByPrimaryKey(9L)).thenReturn(new MemberExt());
    when(userRolesMapper.countByExample(any())).thenReturn(1L);
    when(userRolesMapper.selectByExample(any())).thenReturn(List.of(role()));

    LoginVO result = service.wxLogin(dto);

    assertNotNull(result.getToken());
    verify(redisTemplate).delete("sms:13812345678");
  }

  @Test
  void sendCode() {
    Map<String, Object> result = service.sendCode("13812345678");

    assertEquals(5, result.get("expireMinutes"));
    assertNotNull(result.get("code"));
    verify(valueOperations).set(eq("sms:13812345678"), any(), eq(5L), any());
  }

  @Test
  void register() {
    RegisterDTO dto = new RegisterDTO();
    dto.setPhone("13812345678");
    dto.setPassword("secret1");
    dto.setCode("123456");
    dto.setNickname("猫友");
    when(valueOperations.get("sms:13812345678")).thenReturn("123456");
    when(usersMapper.selectByExample(any())).thenReturn(List.of());
    doAnswer(inv -> {
      Users u = inv.getArgument(0);
      u.setUserId(9L);
      return 1;
    }).when(usersMapper).insertSelective(any(Users.class));
    when(memberExtMapper.selectByPrimaryKey(9L)).thenReturn(new MemberExt());
    when(userRolesMapper.selectByExample(any())).thenReturn(List.of(role()));

    LoginVO result = service.register(dto);

    assertNotNull(result.getToken());
    verify(memberExtMapper).insertSelective(any());
    verify(userRolesMapper).insertSelective(any());
  }

  @Test
  void phoneLogin() {
    PhoneLoginDTO dto = new PhoneLoginDTO();
    dto.setPhone("13812345678");
    dto.setPassword("secret1");
    Users user = user();
    user.setPasswordHash(BCrypt.hashpw("secret1", BCrypt.gensalt()));
    when(usersMapper.selectByExample(any())).thenReturn(List.of(user));
    when(memberExtMapper.selectByPrimaryKey(9L)).thenReturn(new MemberExt());
    when(userRolesMapper.selectByExample(any())).thenReturn(List.of(role()));

    LoginVO result = service.phoneLogin(dto);

    assertNotNull(result.getToken());
  }

  private Users user() {
    Users user = new Users();
    user.setUserId(9L);
    user.setPhone("13812345678");
    user.setNickname("猫友");
    user.setStatus((short) 1);
    return user;
  }

  private UserRoles role() {
    UserRoles role = new UserRoles();
    role.setUserId(9L);
    role.setRoleId(5);
    return role;
  }
}
