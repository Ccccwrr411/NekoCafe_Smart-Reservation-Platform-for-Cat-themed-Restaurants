package cn.edu.bjfu.nekocafe.service.impl;

import cn.edu.bjfu.nekocafe.entity.Stores;
import cn.edu.bjfu.nekocafe.mapper.StoresMapper;
import cn.edu.bjfu.nekocafe.vo.StoreVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StoreServiceImplTest {
  private StoreServiceImpl service;
  private StoresMapper storesMapper;

  @BeforeEach
  void setUp() {
    service = new StoreServiceImpl();
    storesMapper = mock(StoresMapper.class);
    ReflectionTestUtils.setField(service, "storesMapper", storesMapper);
  }

  @Test
  void listStores() {
    Stores store = new Stores();
    store.setStoreId(1);
    store.setName("海淀猫咖");
    store.setAddress("北京市海淀区");
    store.setLongitude(new BigDecimal("116.30"));
    store.setLatitude(new BigDecimal("39.99"));
    store.setStatus((short) 1);
    store.setBusinessHours("10:00-22:00");
    store.setImageUrl("store.png");
    when(storesMapper.selectByExample(any())).thenReturn(List.of(store));

    List<StoreVO> stores = service.listStores();

    assertEquals(1, stores.size());
    assertEquals("海淀猫咖", stores.get(0).getName());
    assertEquals("open", stores.get(0).getStatus());
    verify(storesMapper).selectByExample(any());
  }
}
