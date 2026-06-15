package cn.edu.bjfu.nekocafe.service.impl;

import cn.edu.bjfu.nekocafe.dto.BatchSendCouponDTO;
import cn.edu.bjfu.nekocafe.dto.CreatePromotionDTO;
import cn.edu.bjfu.nekocafe.dto.SendCouponDTO;
import cn.edu.bjfu.nekocafe.entity.Promotions;
import cn.edu.bjfu.nekocafe.entity.Stores;
import cn.edu.bjfu.nekocafe.mapper.*;
import cn.edu.bjfu.nekocafe.vo.StoresOverviewVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class HqServiceImplTest {
  private StoresMapper storesMapper;
  private ReservationsMapper reservationsMapper;
  private PaymentsMapper paymentsMapper;
  private TablesMapper tablesMapper;
  private UsersMapper usersMapper;
  private ReviewsMapper reviewsMapper;
  private MemberExtMapper memberExtMapper;
  private PromotionsMapper promotionsMapper;
  private UserCouponsMapper userCouponsMapper;
  private UserRolesMapper userRolesMapper;

  private HqServiceImpl service() {
    HqServiceImpl service = new HqServiceImpl();
    storesMapper = mock(StoresMapper.class);
    reservationsMapper = mock(ReservationsMapper.class);
    paymentsMapper = mock(PaymentsMapper.class);
    tablesMapper = mock(TablesMapper.class);
    usersMapper = mock(UsersMapper.class);
    reviewsMapper = mock(ReviewsMapper.class);
    memberExtMapper = mock(MemberExtMapper.class);
    promotionsMapper = mock(PromotionsMapper.class);
    userCouponsMapper = mock(UserCouponsMapper.class);
    userRolesMapper = mock(UserRolesMapper.class);
    ReflectionTestUtils.setField(service, "storesMapper", storesMapper);
    ReflectionTestUtils.setField(service, "reservationsMapper", reservationsMapper);
    ReflectionTestUtils.setField(service, "paymentsMapper", paymentsMapper);
    ReflectionTestUtils.setField(service, "tablesMapper", tablesMapper);
    ReflectionTestUtils.setField(service, "usersMapper", usersMapper);
    ReflectionTestUtils.setField(service, "reviewsMapper", reviewsMapper);
    ReflectionTestUtils.setField(service, "memberExtMapper", memberExtMapper);
    ReflectionTestUtils.setField(service, "promotionsMapper", promotionsMapper);
    ReflectionTestUtils.setField(service, "userCouponsMapper", userCouponsMapper);
    ReflectionTestUtils.setField(service, "userRolesMapper", userRolesMapper);
    return service;
  }

  private Promotions promotion() {
    Promotions p = new Promotions();
    p.setPromoId(10);
    p.setName("活动");
    p.setType("VOUCHER");
    p.setRuleJson("{\"reduction\":20}");
    p.setStartTime(new Date(System.currentTimeMillis() + 3600_000L));
    p.setEndTime(new Date(System.currentTimeMillis() + 7200_000L));
    p.setIsActive(true);
    return p;
  }

  private CreatePromotionDTO promotionDTO() {
    CreatePromotionDTO dto = new CreatePromotionDTO();
    dto.setName("活动");
    dto.setType("VOUCHER");
    dto.setRuleJson(Map.of("reduction", 20, "min_spend", 100));
    dto.setStartTime(new Date(System.currentTimeMillis() + 3600_000L));
    dto.setEndTime(new Date(System.currentTimeMillis() + 7200_000L));
    dto.setIsActive(true);
    return dto;
  }

  @Test
  void getStoresOverview() {
    HqServiceImpl service = service();
    Stores store = new Stores();
    store.setStoreId(1);
    store.setName("海淀猫咖");
    store.setStatus((short) 1);
    when(storesMapper.selectByExample(any())).thenReturn(List.of(store));
    when(paymentsMapper.sumTodayPaidByStoreId(1)).thenReturn(1000L);
    when(reservationsMapper.countTodayCompletedByStoreId(1)).thenReturn(5L);
    when(tablesMapper.countActiveByStoreId(1)).thenReturn(10);
    when(tablesMapper.countOccupiedByStoreId(1)).thenReturn(4);
    when(usersMapper.selectManagerByStoreId(1)).thenReturn(Map.of("real_name", "店长", "phone", "13812345678"));
    when(reviewsMapper.avgRatingByStoreId(1)).thenReturn(4.86);
    when(memberExtMapper.countByExample(null)).thenReturn(20L);

    StoresOverviewVO result = service.getStoresOverview();

    assertEquals(1, result.getStores().size());
    assertEquals(1000, result.getTotalRevenue());
    assertEquals(20, result.getTotalMembers());
  }

  @Test
  void refreshDailyStats() {
    assertThrows(UnsupportedOperationException.class, () -> service().refreshDailyStats(new Date()));
  }

  @Test
  void listPromotions() {
    HqServiceImpl service = service();
    when(promotionsMapper.selectByExample(any())).thenReturn(List.of(promotion()));

    assertEquals(1, service.listPromotions("VOUCHER", true, 1, 10).size());
  }

  @Test
  void countPromotions() {
    HqServiceImpl service = service();
    when(promotionsMapper.countByExample(any())).thenReturn(3L);

    assertEquals(3L, service.countPromotions("VOUCHER", true));
  }

  @Test
  void createPromotion() {
    HqServiceImpl service = service();
    Promotions result = service.createPromotion(promotionDTO());

    assertEquals("活动", result.getName());
    verify(promotionsMapper).insertSelective(result);
  }

  @Test
  void updatePromotion() {
    HqServiceImpl service = service();
    Promotions existing = promotion();
    when(promotionsMapper.selectByPrimaryKey(10)).thenReturn(existing);
    CreatePromotionDTO dto = promotionDTO();
    dto.setName("新活动");

    Promotions result = service.updatePromotion(10, dto);

    assertEquals("新活动", result.getName());
    verify(promotionsMapper).updateByPrimaryKeySelective(existing);
  }

  @Test
  void togglePromotion() {
    HqServiceImpl service = service();
    Promotions p = promotion();
    when(promotionsMapper.selectByPrimaryKey(10)).thenReturn(p);

    service.togglePromotion(10);

    assertFalse(p.getIsActive());
    verify(promotionsMapper).updateByPrimaryKeySelective(p);
  }

  @Test
  void deletePromotion() {
    HqServiceImpl service = service();
    when(promotionsMapper.selectByPrimaryKey(10)).thenReturn(promotion());
    when(userCouponsMapper.countByExample(any())).thenReturn(0L);

    service.deletePromotion(10);

    verify(promotionsMapper).deleteByPrimaryKey(10);
  }

  @Test
  void sendCoupon() {
    HqServiceImpl service = service();
    when(promotionsMapper.selectByPrimaryKey(10)).thenReturn(promotion());
    when(userCouponsMapper.selectByExample(any())).thenReturn(List.of());
    SendCouponDTO dto = new SendCouponDTO();
    dto.setPromoId(10);
    dto.setUserId(9L);

    Map<String, Object> result = service.sendCoupon(dto);

    assertEquals(true, result.get("success"));
    verify(userCouponsMapper).insertSelective(any());
  }

  @Test
  void batchSendCoupon() {
    HqServiceImpl service = service();
    when(promotionsMapper.selectByPrimaryKey(10)).thenReturn(promotion());
    when(userCouponsMapper.selectByExample(any())).thenReturn(List.of());
    BatchSendCouponDTO dto = new BatchSendCouponDTO();
    dto.setPromoId(10);
    dto.setTargetType("user");
    dto.setUsersIds(List.of(9L, 10L));

    Map<String, Object> result = service.batchSendCoupon(dto);

    assertEquals(2, result.get("success"));
    assertEquals(2, result.get("total"));
    verify(userCouponsMapper, times(2)).insertSelective(any());
  }
  @Test
  @DisplayName("测试：updatePromotion - 覆盖已开始活动修改规则的拦截")
  void updatePromotion_Started_Fail() {
    HqServiceImpl service = service();
    Promotions existing = promotion();
    // 把开始时间设置为昨天
    existing.setStartTime(new Date(System.currentTimeMillis() - 86400000L));
    when(promotionsMapper.selectByPrimaryKey(10)).thenReturn(existing);

    CreatePromotionDTO dto = promotionDTO();
    dto.setRuleJson(Map.of("reduction", 30)); // 尝试修改规则

    assertThrows(RuntimeException.class, () -> service.updatePromotion(10, dto));
  }

  @Test
  @DisplayName("测试：deletePromotion - 覆盖已有领取记录时的逻辑")
  void deletePromotion_WithClaimed() {
    HqServiceImpl service = service();
    when(promotionsMapper.selectByPrimaryKey(10)).thenReturn(promotion());
    when(userCouponsMapper.countByExample(any())).thenReturn(5L); // 模拟已有5人领取

    assertThrows(RuntimeException.class, () -> service.deletePromotion(10));
    verify(promotionsMapper).updateByPrimaryKeySelective(any()); // 验证是否自动触发了停用
  }

  @Test
  @DisplayName("测试：validatePromotionDTO - 覆盖各种错误参数校验")
  void validatePromotionDTO_Failures() {
    HqServiceImpl service = service();

    // 1. 测试无效类型：直接定义并使用 dto1
    CreatePromotionDTO dto1 = promotionDTO();
    dto1.setType("WRONG");
    assertThrows(RuntimeException.class, () -> service.createPromotion(dto1));

    // 2. 测试折扣 < 0：直接定义并使用 dto2
    CreatePromotionDTO dto2 = promotionDTO();
    dto2.setType("DISCOUNT");
    dto2.setRuleJson(Map.of("discount", -0.5));
    assertThrows(RuntimeException.class, () -> service.createPromotion(dto2));

    // 3. 测试时间逻辑：直接定义并使用 dto3
    CreatePromotionDTO dto3 = promotionDTO();
    dto3.setStartTime(new Date());
    dto3.setEndTime(new Date(System.currentTimeMillis() - 1000)); // 结束在开始前
    assertThrows(RuntimeException.class, () -> service.createPromotion(dto3));
  }

  @Test
  @DisplayName("测试：batchSendCoupon - 覆盖多种TargetType")
  void batchSendCoupon_Types() {
    HqServiceImpl service = service();
    when(promotionsMapper.selectByPrimaryKey(10)).thenReturn(promotion());

    BatchSendCouponDTO dto = new BatchSendCouponDTO();
    dto.setPromoId(10);
    dto.setTargetType("role");
    dto.setTargetValue("1");

    // 覆盖按角色发券逻辑
    when(userRolesMapper.selectByExample(any())).thenReturn(Collections.emptyList());
    assertThrows(RuntimeException.class, () -> service.batchSendCoupon(dto));
  }
}
