package cn.edu.bjfu.nekocafe.service.impl;

import cn.edu.bjfu.nekocafe.dto.PromotionCalcDTO;
import cn.edu.bjfu.nekocafe.entity.Promotions;
import cn.edu.bjfu.nekocafe.entity.UserCoupons;
import cn.edu.bjfu.nekocafe.mapper.PromotionsMapper;
import cn.edu.bjfu.nekocafe.mapper.UserCouponsMapper;
import cn.edu.bjfu.nekocafe.vo.CouponVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CouponServiceImplTest {
  private CouponServiceImpl service;
  private UserCouponsMapper userCouponsMapper;
  private PromotionsMapper promotionsMapper;

  @BeforeEach
  void setUp() {
    service = new CouponServiceImpl();
    userCouponsMapper = mock(UserCouponsMapper.class);
    promotionsMapper = mock(PromotionsMapper.class);
    ReflectionTestUtils.setField(service, "userCouponsMapper", userCouponsMapper);
    ReflectionTestUtils.setField(service, "promotionsMapper", promotionsMapper);
    UserCoupons uc = new UserCoupons();
    uc.setCouponId(100L);
    uc.setPromoId(10);
    uc.setStatus("unused");
    when(userCouponsMapper.selectByExample(any())).thenReturn(List.of(uc));
    when(promotionsMapper.selectByPrimaryKey(10)).thenReturn(promotion());
  }

  @Test
  void listCoupons() {
    List<CouponVO> result = service.listCoupons(9L);

    assertEquals(1, result.size());
    assertEquals("100", result.get(0).getId());
    assertEquals("cashback", result.get(0).getType());
  }

  @Test
  void listAvailableCoupons() {
    List<CouponVO> result = service.listAvailableCoupons(1, 100, 9L);

    assertEquals(1, result.size());
    assertEquals(20, result.get(0).getSaving());
  }

  @Test
  void getPromotionRules() {
    when(promotionsMapper.selectByExample(any())).thenReturn(List.of(promotion()));

    Map<String, Object> result = service.getPromotionRules();

    assertEquals(1, ((List<?>) result.get("activePromotions")).size());
    assertNotNull(result.get("stackingRules"));
  }

  @Test
  void calculatePromotion() {
    PromotionCalcDTO dto = new PromotionCalcDTO();
    dto.setAmount(100);
    dto.setCouponIds(List.of("100"));

    Map<String, Object> result = service.calculatePromotion(9L, dto);

    assertEquals(100, result.get("originalAmount"));
    assertEquals(20, result.get("totalDiscount"));
    assertEquals(80, result.get("finalAmount"));
  }

  private Promotions promotion() {
    Promotions p = new Promotions();
    p.setPromoId(10);
    p.setName("满100减20");
    p.setType("cashback");
    p.setRuleJson("{\"value\":20,\"minAmount\":50}");
    p.setIsActive(true);
    return p;
  }
}
