package cn.edu.bjfu.nekocafe.service.impl;

import cn.edu.bjfu.nekocafe.entity.Dishes;
import cn.edu.bjfu.nekocafe.entity.StoreDishes;
import cn.edu.bjfu.nekocafe.mapper.DishesMapper;
import cn.edu.bjfu.nekocafe.mapper.StoreDishesMapper;
import cn.edu.bjfu.nekocafe.vo.MenuVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MenuServiceImplTest {
  private MenuServiceImpl service;
  private DishesMapper dishesMapper;
  private StoreDishesMapper storeDishesMapper;
  private RedisTemplate<String, Object> redisTemplate;
  private ValueOperations<String, Object> valueOperations;

  @BeforeEach
  void setUp() {
    service = new MenuServiceImpl();
    dishesMapper = mock(DishesMapper.class);
    storeDishesMapper = mock(StoreDishesMapper.class);
    redisTemplate = mock(RedisTemplate.class);
    valueOperations = mock(ValueOperations.class);
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    ReflectionTestUtils.setField(service, "dishesMapper", dishesMapper);
    ReflectionTestUtils.setField(service, "storeDishesMapper", storeDishesMapper);
    ReflectionTestUtils.setField(service, "redisTemplate", redisTemplate);
    ReflectionTestUtils.setField(service, "objectMapper", new ObjectMapper());
  }

  @Test
  void getMenu() {
    when(valueOperations.get(anyString())).thenReturn(null);
    StoreDishes sd = new StoreDishes();
    sd.setStoreId(1);
    sd.setDishId(10);
    sd.setPriceOverride(new BigDecimal("26"));
    when(storeDishesMapper.selectByExample(any())).thenReturn(List.of(sd));
    Dishes dish = new Dishes();
    dish.setDishId(10);
    dish.setName("拿铁");
    dish.setCategory("特色咖啡");
    dish.setDescription("香浓");
    dish.setPrice(new BigDecimal("30"));
    dish.setTags("热销,新品");
    when(dishesMapper.selectByExample(any())).thenReturn(List.of(dish));

    MenuVO result = service.getMenu(1);

    assertEquals(1, result.getCategories().size());
    assertEquals(1, result.getItems().size());
    assertEquals(26, result.getItems().get(0).getPrice());
    verify(valueOperations).set(anyString(), anyString(), eq(12L), eq(TimeUnit.HOURS));
  }
}
