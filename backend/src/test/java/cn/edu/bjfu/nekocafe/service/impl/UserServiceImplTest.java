package cn.edu.bjfu.nekocafe.service.impl;

import cn.edu.bjfu.nekocafe.dto.RealnameDTO;
import cn.edu.bjfu.nekocafe.dto.UserUpdateDTO;
import cn.edu.bjfu.nekocafe.entity.*;
import cn.edu.bjfu.nekocafe.mapper.*;
import cn.edu.bjfu.nekocafe.vo.UserProfileVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

  @Mock private UsersMapper usersMapper;
  @Mock private MemberExtMapper memberExtMapper;
  @Mock private UserCouponsMapper userCouponsMapper;
  @Mock private ReservationsMapper reservationsMapper;
  @Mock private StoresMapper storesMapper;
  @Mock private UserRolesMapper userRolesMapper;

  @InjectMocks
  private UserServiceImpl service;

  @Test
  @DisplayName("测试：获取用户全量画像 (满血覆盖所有关联表和等级计算)")
  void getProfile_Success() {
    // 1. 模拟用户基础信息
    Users user = new Users();
    user.setUserId(9L);
    user.setNickname("猫奴一号");
    user.setPhone("13800138000"); // 测试脱敏
    user.setEmail("test@test.com");
    user.setCreatedAt(new Date());
    user.setIsVerified(true);
    user.setRealName("张三");
    user.setIdCard("110105199001011234"); // 测试身份证脱敏
    when(usersMapper.selectByPrimaryKey(9L)).thenReturn(user);

    // 2. 模拟会员信息 (测试计算等级)
    MemberExt me = new MemberExt();
    me.setLevel(2);
    me.setTotalPoints(6500); // 应该计算为 4 级
    me.setCumulativeAmount(new BigDecimal("1000"));
    lenient().when(memberExtMapper.selectByPrimaryKey(9L)).thenReturn(me);

    // 3. 模拟各种统计数量
    lenient().when(reservationsMapper.countByExample(any())).thenReturn(5L);
    lenient().when(userCouponsMapper.countByExample(any())).thenReturn(3L);

    // 4. 模拟关联门店查询
    UserRoles ur = new UserRoles(); ur.setStoreId(1);
    lenient().when(userRolesMapper.selectByExample(any())).thenReturn(Collections.singletonList(ur));
    Stores store = new Stores(); store.setName("五道口店");
    lenient().when(storesMapper.selectByPrimaryKey(1)).thenReturn(store);

    UserProfileVO result = service.getProfile(9L);

    assertNotNull(result);
    assertEquals("中级会员", result.getMemberLevel()); // 验证 6500 分自动升级到 4级
    assertEquals("138****8000", result.getPhone()); // 验证手机脱敏
    assertTrue(result.getIdCardMask().contains("********")); // 验证身份证脱敏
    assertEquals("五道口店", result.getStoreName());
  }

  @Test
  @DisplayName("测试：实名认证")
  void verifyRealname_Success() {
    RealnameDTO dto = new RealnameDTO();
    dto.setRealName("李四");
    dto.setIdCard("110105199001018888");

    lenient().when(usersMapper.updateByPrimaryKeySelective(any())).thenReturn(1);

    Map<String, Object> result = service.verifyRealname(9L, dto);
    assertTrue((Boolean) result.get("verified"));
    assertEquals("李四", result.get("realName"));
  }

  @Test
  @DisplayName("测试：修改个人资料 (覆盖正则校验)")
  void updateProfile_Success() {
    Users user = new Users(); user.setUserId(9L);
    when(usersMapper.selectByPrimaryKey(9L)).thenReturn(user);
    lenient().when(usersMapper.updateByPrimaryKeySelective(any())).thenReturn(1);

    UserUpdateDTO dto = new UserUpdateDTO();
    dto.setNickName("新昵称");
    dto.setAvatarUrl("new_url");
    dto.setPhone("13912345678"); // 格式正确
    dto.setEmail("abc@def.com"); // 格式正确

    // 因为 updateProfile 内部调了 getProfile，需要给 getProfile 提供基础 mock
    lenient().when(memberExtMapper.selectByPrimaryKey(anyLong())).thenReturn(null);
    lenient().when(reservationsMapper.countByExample(any())).thenReturn(0L);
    lenient().when(userCouponsMapper.countByExample(any())).thenReturn(0L);
    lenient().when(userRolesMapper.selectByExample(any())).thenReturn(Collections.emptyList());

    UserProfileVO result = service.updateProfile(9L, dto);
    assertNotNull(result);
  }

  @Test
  @DisplayName("测试：修改密码 (包含旧密码校验)")
  void changePassword_Success() {
    Users user = new Users();
    user.setUserId(9L);
    // 使用 BCrypt 生成一个真实的 hash，明文是 "old123"
    user.setPasswordHash(BCrypt.hashpw("old123", BCrypt.gensalt()));
    when(usersMapper.selectByPrimaryKey(9L)).thenReturn(user);

    lenient().when(usersMapper.updateByPrimaryKeySelective(any())).thenReturn(1);

    Map<String, Object> result = service.changePassword(9L, "old123", "new45678");
    assertTrue((Boolean) result.get("success"));
  }
}
