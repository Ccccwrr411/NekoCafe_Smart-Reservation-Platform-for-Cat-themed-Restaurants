// package cn.edu.bjfu.nekocafe.service.impl;

// import cn.edu.bjfu.nekocafe.entity.*;
// import cn.edu.bjfu.nekocafe.mapper.*;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.springframework.data.redis.core.RedisTemplate;
// import org.springframework.data.redis.core.ValueOperations;

// import java.math.BigDecimal;
// import java.util.*;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.*;
// import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class)
// class RecommendServiceImplTest {

//   @Mock private MemberExtMapper memberExtMapper;
//   @Mock private CatProfilesMapper catProfilesMapper;
//   @Mock private DishesMapper dishesMapper;
//   @Mock private OrderItemsMapper orderItemsMapper;
//   @Mock private ReservationsMapper reservationsMapper;
//   @Mock private TablesMapper tablesMapper;
//   @Mock private RedisTemplate<String, Object> redisTemplate;
//   @Mock private ValueOperations<String, Object> valueOperations;

//   @InjectMocks
//   private RecommendServiceImpl service;

//   // ==========================================
//   // 测试 1: 异常参数处理
//   // ==========================================
//   @Test
//   @DisplayName("测试：用户ID为空")
//   void recommend_UserIdNull() {
//     Map<String, Object> result = service.recommend(null, 2, false);
//     assertNotNull(result);
//     assertTrue(result.get("reason").toString().contains("不能为空"));
//   }

//   // ==========================================
//   // 测试 2: 缓存命中逻辑 (最快路径)
//   // ==========================================
//   @Test
//   @DisplayName("测试：Redis 缓存命中")
//   void recommend_CacheHit() {
//     lenient().when(redisTemplate.hasKey(anyString())).thenReturn(true);
//     lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);

//     // 模拟 Redis 中存的 JSON 字符串
//     String mockCacheJson = "{\"reason\":\"缓存推荐\",\"cats\":[],\"dishes\":[],\"tables\":[]}";
//     lenient().when(valueOperations.get(anyString())).thenReturn(mockCacheJson);

//     Map<String, Object> result = service.recommend(1L, 2, false);
//     assertEquals("缓存推荐", result.get("reason"));
//   }

//   // ==========================================
//   // 测试 3: 新用户冷启动逻辑
//   // ==========================================
//   @Test
//   @DisplayName("测试：新用户/冷启动 (触发全局热门兜底)")
//   void recommend_NewUser() {
//     // 模拟未命中缓存
//     lenient().when(redisTemplate.hasKey(anyString())).thenReturn(false);

//     // 模拟新用户：无画像、无订单记录
//     lenient().when(memberExtMapper.selectByPrimaryKey(anyLong())).thenReturn(null);
//     lenient().when(reservationsMapper.selectByUserIdAndStatuses(anyLong(), anyList()))
//       .thenReturn(Collections.emptyList());

//     // 模拟全店猫咪
//     CatProfiles cat = new CatProfiles();
//     cat.setCatId(1);
//     cat.setName("小白");
//     lenient().when(catProfilesMapper.selectByExample(any())).thenReturn(Collections.singletonList(cat));

//     // 模拟热门菜品
//     Map<String, Object> hotDish = new HashMap<>();
//     hotDish.put("dishid", 101);
//     hotDish.put("ordercount", 100);
//     lenient().when(dishesMapper.selectHotDishesByCategory(anyInt())).thenReturn(Collections.singletonList(hotDish));

//     Dishes d = new Dishes();
//     d.setDishId(101);
//     d.setName("热门猫条");
//     d.setCategory("零食");
//     lenient().when(dishesMapper.selectByPrimaryKey(101)).thenReturn(d);

//     // 模拟无桌位，触发降级描述表
//     lenient().when(tablesMapper.selectAvailableTablesForRecommend(anyInt(), any(), anyInt()))
//       .thenReturn(Collections.emptyList());

//     Map<String, Object> result = service.recommend(2L, 1, false);

//     assertNotNull(result);
//     assertTrue(result.get("reason").toString().contains("人气推荐"));
//     assertFalse(((List<?>) result.get("cats")).isEmpty());
//     assertFalse(((List<?>) result.get("dishes")).isEmpty());
//     assertFalse(((List<?>) result.get("tables")).isEmpty()); // 降级推荐
//   }

//   // ==========================================
//   // 测试 4: 活跃 VIP 老用户 (覆盖各种复杂规则)
//   // ==========================================
//   @Test
//   @DisplayName("测试：VIP 活跃用户 (触发品种匹配/复购打标/温顺猫/搭配推荐/VIP包厢)")
//   void recommend_VipActiveUser() {
//     lenient().when(redisTemplate.hasKey(anyString())).thenReturn(false);
//     lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations); // 防止写入缓存时NPE

//     // 1. 模拟画像: VIP 4级，喜欢英短和甜食
//     MemberExt ext = new MemberExt();
//     ext.setLevel(4);
//     ext.setPreferences("{\"favoriteBreeds\":[\"英短\"],\"flavorPreference\":\"甜\"}");
//     ext.setLastVisitTime(new Date());
//     lenient().when(memberExtMapper.selectByPrimaryKey(3L)).thenReturn(ext);

//     // 2. 模拟订单: 30天内有多次订单 (触发新品打标 R14)
//     Reservations r1 = new Reservations();
//     r1.setReservationTime(new Date()); // 今天
//     Reservations r2 = new Reservations();
//     r2.setReservationTime(new Date());
//     Reservations r3 = new Reservations();
//     r3.setReservationTime(new Date());
//     lenient().when(reservationsMapper.selectByUserIdAndStatuses(eq(3L), anyList()))
//       .thenReturn(Arrays.asList(r1, r2, r3));

//     // 3. 模拟猫咪: 触发 R3(英短匹配) 和 R11(带小孩加分)
//     CatProfiles cat1 = new CatProfiles();
//     cat1.setCatId(1);
//     cat1.setBreed("英国短毛猫"); // 别名匹配 "英短"
//     CatProfiles cat2 = new CatProfiles();
//     cat2.setCatId(2);
//     cat2.setPersonality("温顺 活泼");
//     lenient().when(catProfilesMapper.selectByExample(any())).thenReturn(Arrays.asList(cat1, cat2));
//     lenient().when(catProfilesMapper.selectByPersonality("温顺")).thenReturn(Collections.singletonList(cat2));

//     // 4. 模拟菜品: 触发各种匹配
//     // 历史点单
//     Map<String, Object> historyDish = new HashMap<>();
//     historyDish.put("dishid", 201);
//     lenient().when(orderItemsMapper.selectDishFrequencyByUserId(3L)).thenReturn(Collections.singletonList(historyDish));
//     // 协同过滤
//     Map<String, Object> collabDish = new HashMap<>();
//     collabDish.put("dishid", 202);
//     lenient().when(orderItemsMapper.selectCollaborativeFilterDishes(anyLong(), anyString(), anyInt()))
//       .thenReturn(Collections.singletonList(collabDish));
//     // 标签匹配
//     Dishes d3 = new Dishes();
//     d3.setDishId(203);
//     d3.setTags("甜 新品"); // 触发 R14 复购打标
//     lenient().when(dishesMapper.selectByTagKeyword("甜")).thenReturn(Collections.singletonList(d3));

//     // 补全菜品详情
//     Dishes d1 = new Dishes(); d1.setDishId(201); d1.setCategory("主食");
//     Dishes d2 = new Dishes(); d2.setDishId(202); d2.setCategory("咖啡"); // 触发 R19 搭配甜品
//     lenient().when(dishesMapper.selectByPrimaryKey(201)).thenReturn(d1);
//     lenient().when(dishesMapper.selectByPrimaryKey(202)).thenReturn(d2);
//     lenient().when(dishesMapper.selectByPrimaryKey(203)).thenReturn(d3);

//     // 搭配甜品兜底
//     Dishes dessert = new Dishes(); dessert.setDishId(999); dessert.setName("焦糖布丁"); dessert.setCategory("甜品");
//     lenient().when(dishesMapper.selectFirstActiveDishByCategory("甜品")).thenReturn(dessert);

//     // 5. 模拟桌位: 触发 VIP 包厢
//     Tables vipTable = new Tables();
//     vipTable.setTableId(10);
//     vipTable.setTableType("vip");
//     vipTable.setTableNo("V01");
//     lenient().when(tablesMapper.selectAvailableTablesForRecommend(eq(2), eq("vip"), anyInt()))
//       .thenReturn(Collections.singletonList(vipTable));
//     lenient().when(tablesMapper.selectAvailableTablesForRecommend(eq(2), isNull(), anyInt()))
//       .thenReturn(Collections.emptyList());

//     // 执行测试: 用户ID=3, 同行人数=2, 带有小孩=true
//     Map<String, Object> result = service.recommend(3L, 2, true);

//     assertNotNull(result);

//     // 验证结果集是否包含预期元素
//     List<Map<String, Object>> cats = (List<Map<String, Object>>) result.get("cats");
//     assertFalse(cats.isEmpty());

//     List<Map<String, Object>> dishes = (List<Map<String, Object>>) result.get("dishes");
//     assertFalse(dishes.isEmpty());

//     List<Map<String, Object>> tables = (List<Map<String, Object>>) result.get("tables");
//     assertEquals(10, tables.get(0).get("tableId")); // 首推应该是 VIP 桌位
//   }

//   // ==========================================
//   // 测试 5: 沉睡用户召回
//   // ==========================================
//   @Test
//   @DisplayName("测试：沉睡用户 (触发特惠召回套餐)")
//   void recommend_SleepingUser() {
//     lenient().when(redisTemplate.hasKey(anyString())).thenReturn(false);

//     // 模拟 100 天没来过的用户
//     MemberExt ext = new MemberExt();
//     Calendar cal = Calendar.getInstance();
//     cal.add(Calendar.DAY_OF_YEAR, -100);
//     ext.setLastVisitTime(cal.getTime());
//     lenient().when(memberExtMapper.selectByPrimaryKey(4L)).thenReturn(ext);

//     Reservations r1 = new Reservations();
//     r1.setReservationTime(cal.getTime());
//     lenient().when(reservationsMapper.selectByUserIdAndStatuses(eq(4L), anyList()))
//       .thenReturn(Collections.singletonList(r1));

//     Map<String, Object> result = service.recommend(4L, 1, false);

//     assertNotNull(result);
//     List<Map<String, Object>> dishes = (List<Map<String, Object>>) result.get("dishes");
//     // 应该有一条 dishId = -1 的限时特惠召回数据
//     boolean hasRecallPromo = dishes.stream().anyMatch(d -> Integer.valueOf(-1).equals(d.get("dishId")));
//     assertTrue(hasRecallPromo, "沉睡用户应该被推荐召回套餐");
//   }
// }
