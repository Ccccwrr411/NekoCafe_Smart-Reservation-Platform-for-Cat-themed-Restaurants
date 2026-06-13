package cn.edu.bjfu.nekocafe.service.impl;

import cn.edu.bjfu.nekocafe.entity.Dishes;
import cn.edu.bjfu.nekocafe.entity.DishesExample;
import cn.edu.bjfu.nekocafe.entity.StoreDishes;
import cn.edu.bjfu.nekocafe.entity.StoreDishesExample;
import cn.edu.bjfu.nekocafe.mapper.DishesMapper;
import cn.edu.bjfu.nekocafe.mapper.StoreDishesMapper;
import cn.edu.bjfu.nekocafe.service.MenuService;
import cn.edu.bjfu.nekocafe.vo.MenuVO;
import cn.edu.bjfu.nekocafe.vo.MenuVO.CategoryVO;
import cn.edu.bjfu.nekocafe.vo.MenuVO.MenuItemVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 菜单服务实现
 * 负责人：B同学
 *
 * 实现接口 D-1：GET /api/menu?storeId={storeId}
 * 使用 Redis 缓存菜品列表，缓存 key 为 nekocafe:menu:store:v2:{storeId}，过期时间 12 小时
 */
@Service
public class MenuServiceImpl implements MenuService {

    private static final String CACHE_KEY_PREFIX = "nekocafe:menu:store:v2:";
    private static final long CACHE_TTL_HOURS = 12;

    /** 分类名 → emoji 图标映射 */
    private static final Map<String, String> CATEGORY_ICONS = new LinkedHashMap<>();
    static {
        CATEGORY_ICONS.put("招牌饮品", "☕");
        CATEGORY_ICONS.put("特色咖啡", "☕");
        CATEGORY_ICONS.put("茶饮", "🍵");
        CATEGORY_ICONS.put("甜品", "🍰");
        CATEGORY_ICONS.put("甜点", "🍰");
        CATEGORY_ICONS.put("轻食", "🥪");
        CATEGORY_ICONS.put("小食", "🍟");
        CATEGORY_ICONS.put("简餐", "🍱");
    }

    @Autowired
    private DishesMapper dishesMapper;

    @Autowired
    private StoreDishesMapper storeDishesMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public MenuVO getMenu(Integer storeId) {
        String cacheKey = CACHE_KEY_PREFIX + storeId;

        // 1. 先从 Redis 缓存读取
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                String cachedJson = cached.toString();
                return objectMapper.readValue(cachedJson, MenuVO.class);
            }
        } catch (Exception e) {
            // 缓存读取失败（如 JSON 反序列化异常），降级查数据库
        }

        // 2. 缓存未命中，从数据库查询
        MenuVO menu = buildMenuFromDB(storeId);

        // 3. 写入 Redis 缓存
        try {
            String menuJson = objectMapper.writeValueAsString(menu);
            redisTemplate.opsForValue().set(cacheKey, menuJson, CACHE_TTL_HOURS, TimeUnit.HOURS);
        } catch (JsonProcessingException e) {
            // 写入缓存失败不影响正常返回
        }

        return menu;
    }

    /**
     * 从数据库构建菜单数据
     */
    private MenuVO buildMenuFromDB(Integer storeId) {
        MenuVO menu = new MenuVO();

        // 查该门店有哪些菜品（关联表 store_dishes）
        StoreDishesExample sdExample = new StoreDishesExample();
        sdExample.createCriteria()
                .andStoreIdEqualTo(storeId)
                .andIsAvailableEqualTo(true);
        List<StoreDishes> storeDishesList = storeDishesMapper.selectByExample(sdExample);

        if (storeDishesList == null || storeDishesList.isEmpty()) {
            menu.setCategories(new ArrayList<>());
            menu.setItems(new ArrayList<>());
            return menu;
        }

        // 收集 dishId 列表
        List<Integer> dishIds = storeDishesList.stream()
                .map(StoreDishes::getDishId)
                .distinct()
                .collect(Collectors.toList());

        // 批量查菜品详情
        DishesExample dishesExample = new DishesExample();
        dishesExample.createCriteria().andDishIdIn(dishIds);
        List<Dishes> dishesList = dishesMapper.selectByExample(dishesExample);

        // 构建 dishId → Dishes 映射
        Map<Integer, Dishes> dishesMap = new HashMap<>();
        if (dishesList != null) {
            for (Dishes d : dishesList) {
                dishesMap.put(d.getDishId(), d);
            }
        }

        // 构建分类列表（从 category 字段去重）
        Set<String> categoryNames = new LinkedHashSet<>();
        for (Integer dishId : dishIds) {
            Dishes d = dishesMap.get(dishId);
            if (d != null && d.getCategory() != null && !d.getCategory().isEmpty()) {
                categoryNames.add(d.getCategory());
            }
        }

        // 构建 MenuItemVO 列表
        List<MenuItemVO> items = new ArrayList<>();
        for (Integer dishId : dishIds) {
            Dishes d = dishesMap.get(dishId);
            if (d == null) {
                continue;
            }

            MenuItemVO item = new MenuItemVO();
            item.setId(d.getDishId());
            item.setName(d.getName());
            item.setCategoryId(getCategoryId(categoryNames, d.getCategory()));
            item.setDesc(d.getDescription());
            // 直接使用数据库中的完整图片URL，不再拼接
            item.setImageUrl(d.getImageUrl());

            // 价格：优先使用门店覆盖价，否则用菜品原价
            StoreDishes sd = storeDishesList.stream()
                    .filter(s -> s.getDishId().equals(d.getDishId()))
                    .findFirst().orElse(null);
            if (sd != null && sd.getPriceOverride() != null) {
                item.setPrice(sd.getPriceOverride().intValue());
            } else if (d.getPrice() != null) {
                item.setPrice(d.getPrice().intValue());
            } else {
                item.setPrice(0);
            }

            // isHot / isNew：从 tags 字段解析
            String tags = d.getTags();
            item.setIsHot(tags != null && tags.contains("热销"));
            item.setIsNew(tags != null && tags.contains("新品"));

            // sales / rating：数据库暂无对应字段，暂设默认值
            item.setSales(0);
            item.setRating(5.0);

            items.add(item);
        }

        // 构建 CategoryVO 列表
        List<CategoryVO> categories = new ArrayList<>();
        int catId = 1;
        for (String name : categoryNames) {
            CategoryVO cat = new CategoryVO();
            cat.setId(catId++);
            cat.setName(name);
            cat.setIcon(getCategoryIcon(name));
            categories.add(cat);
        }

        menu.setCategories(categories);
        menu.setItems(items);
        return menu;
    }

    /**
     * 根据分类名获取其序号 ID
     */
    private Integer getCategoryId(Set<String> categoryNames, String categoryName) {
        if (categoryName == null) return 0;
        int id = 1;
        for (String name : categoryNames) {
            if (name.equals(categoryName)) {
                return id;
            }
            id++;
        }
        return 0;
    }

    /**
     * 根据分类名获取 emoji 图标
     */
    private String getCategoryIcon(String categoryName) {
        if (categoryName == null) return "📋";
        return CATEGORY_ICONS.getOrDefault(categoryName, "📋");
    }
}
