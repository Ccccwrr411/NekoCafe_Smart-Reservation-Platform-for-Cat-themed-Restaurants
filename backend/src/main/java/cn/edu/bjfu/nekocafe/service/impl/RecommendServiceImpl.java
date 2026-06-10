package cn.edu.bjfu.nekocafe.service.impl;

import cn.edu.bjfu.nekocafe.mapper.CatProfilesMapper;
import cn.edu.bjfu.nekocafe.mapper.DishesMapper;
import cn.edu.bjfu.nekocafe.mapper.MemberExtMapper;
import cn.edu.bjfu.nekocafe.mapper.OrderItemsMapper;
import cn.edu.bjfu.nekocafe.mapper.ReservationsMapper;
import cn.edu.bjfu.nekocafe.mapper.TablesMapper;
import cn.edu.bjfu.nekocafe.service.RecommendService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * AI 推荐服务实现 — 多策略融合专家规则引擎
 *
 * 规则体系（18 条 / 5 维度）：
 *   ┌─────────────────────────────────────────────────────┐
 *   │  A: 冷启动/降级  │ R1 偏好空→热门   R5 新用户→全店Top     │
 *   │                 │ R6 Redis缓存    R17 全店销量兜底      │
 *   ├─────────────────────────────────────────────────────┤
 *   │  B: 个性化匹配  │ R2 口味标签      R3 品种匹配猫咪       │
 *   │                 │ R4 历史点餐加权   R16 同偏好协同过滤    │
 *   ├─────────────────────────────────────────────────────┤
 *   │  C: 上下文感知  │ R7 工作日/周末  R8 午/下午茶/晚餐时段  │
 *   │                 │ R9 季节性        R10 同行人数桌位       │
 *   ├─────────────────────────────────────────────────────┤
 *   │  D: 会员分层    │ R11 小孩→温顺猫  R13 VIP优先包厢       │
 *   │                 │ R14 复购→新品    R15 沉睡→优惠召回     │
 *   ├─────────────────────────────────────────────────────┤
 *   │  E: 业务约束    │ R18 互斥过滤     R19 组合搭配          │
 *   └─────────────────────────────────────────────────────┘
 */
@Service
public class RecommendServiceImpl implements RecommendService {

    // ==================== 品种别名映射表（简称 → 正式全名） ====================
    // 用于 R3 品种匹配：用户偏好可能存"英短"，数据库 breed 可能存"英国短毛猫"
    private static final Map<String, String> BREED_ALIAS_MAP;
    static {
        BREED_ALIAS_MAP = new HashMap<>();
        BREED_ALIAS_MAP.put("英短",   "英国短毛猫");
        BREED_ALIAS_MAP.put("美短",   "美国短毛猫");
        BREED_ALIAS_MAP.put("布偶",   "布偶猫");
        BREED_ALIAS_MAP.put("缅因",   "缅因猫");
        BREED_ALIAS_MAP.put("波斯",   "波斯猫");
        BREED_ALIAS_MAP.put("折耳",   "苏格兰折耳猫");
        BREED_ALIAS_MAP.put("苏折",   "苏格兰折耳猫");
        BREED_ALIAS_MAP.put("橘猫",   "中华田园猫");
        BREED_ALIAS_MAP.put("加菲",   "异国短毛猫");
        BREED_ALIAS_MAP.put("暹罗",   "暹罗猫");
        BREED_ALIAS_MAP.put("蓝猫",   "英国短毛猫");
        BREED_ALIAS_MAP.put("金渐层", "英国短毛猫");
        BREED_ALIAS_MAP.put("银渐层", "英国短毛猫");
    }

    // ==================== 权重配置 ====================
    private static final double W_PERSONAL = 0.45;  // 个性化权重
    private static final double W_CONTEXT  = 0.25;  // 上下文权重
    private static final double W_MEMBER   = 0.15;  // 会员权重
    private static final double W_SOCIAL   = 0.15;  // 社交权重

    // ==================== Top-N 配置 ====================
    private static final int TOP_CATS    = 3;  // 推荐猫数
    private static final int TOP_DISHES  = 5;  // 推荐菜品数
    private static final int TOP_TABLES  = 3;  // 推荐桌位数

    // ==================== 缓存配置 ====================
    private static final String CACHE_KEY_PREFIX = "recommend:";
    private static final long CACHE_TTL_SECONDS = 300;  // 5分钟

    @Autowired
    private MemberExtMapper memberExtMapper;

    @Autowired
    private CatProfilesMapper catProfilesMapper;

    @Autowired
    private DishesMapper dishesMapper;

    @Autowired
    private OrderItemsMapper orderItemsMapper;

    @Autowired
    private ReservationsMapper reservationsMapper;

    @Autowired
    private TablesMapper tablesMapper;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ==================== 主入口 ====================

    @Override
    public Map<String, Object> recommend(Long userId, Integer companionCount, Boolean hasChild) {
        if (userId == null) {
            return buildErrorResult("用户ID不能为空");
        }

        // ---- R6: 缓存优先（最快路径）----
        String cacheKey = CACHE_KEY_PREFIX + userId + ":" + companionCount + ":" + hasChild;
        if (redisTemplate != null && Boolean.TRUE.equals(redisTemplate.hasKey(cacheKey))) {
            try {
                String cached = (String) redisTemplate.opsForValue().get(cacheKey);
                return objectMapper.readValue(cached, Map.class);
            } catch (Exception e) {
                // 缓存读取失败不影响主流程
            }
        }

        // ---- Step1: 加载用户画像 ----
        MemberProfile profile = loadUserProfile(userId);

        // ---- Step2: 猫咪推荐（维度 B-R3 + D-R11）----
        List<Map<String, Object>> cats = recommendCats(profile, hasChild);

        // ---- Step3: 菜品推荐（维度 B-R2/R4/R16 + C-R8/R9）----
        List<Map<String, Object>> dishes = recommendDishes(profile);

        // ---- Step4: 桌位推荐（维度 C-R7/R10 + D-R13）----
        List<Map<String, Object>> tables = recommendTables(profile, companionCount);

        // ---- Step5: 业务后处理（维度 E-R18/R19）----
        dishes = postProcessDishes(dishes);  // R18互斥 + R19搭配

        // ---- Step6: 组装结果 ----
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("reason", buildReason(profile));
        result.put("cats", cats);
        result.put("dishes", dishes);
        result.put("tables", tables);
        result.put("userProfile", buildUserprofileVO(profile));

        // ---- R6: 写入缓存 ----
        writeToCache(cacheKey, result);

        return result;
    }

    // ================================================================
    //  Step1: 用户画像加载
    // ================================================================

    /**
     * 内部用户画像对象，聚合所有推荐需要的数据
     */
    private static class MemberProfile {
        Long userId;
        Integer level;
        Date lastVisitTime;
        String preferencesJson;
        List<String> favoriteBreeds;      // 解析自 preferences.favoriteBreeds
        String flavorPreference;           // 解析自 preferences.flavorPreference
        boolean isNewUser;                // 无历史订单
        int visitCount30d;                // 近30天到店次数（R14）
        boolean isSleepingUser;            // 超60天未到店（R15）
        boolean hasValidPreferences;       // preferences != null 且可解析
    }

    private MemberProfile loadUserProfile(Long userId) {
        MemberProfile p = new MemberProfile();
        p.userId = userId;

        // 查 member_ext
        var ext = memberExtMapper.selectByPrimaryKey(userId);
        if (ext != null) {
            p.level = ext.getLevel() != null ? ext.getLevel() : 1;
            p.lastVisitTime = ext.getLastVisitTime();
            p.preferencesJson = ext.getPreferences();
        } else {
            p.level = 1;
        }

        // 解析 JSON 偏好
        parsePreferences(p);

        // 统计历史行为（R4/R14/R15）
        analyzeHistory(p);

        return p;
    }

    /**
     * 解析 member_ext.preferences JSON
     * 预期格式: {"favoriteBreeds":["英短","美短"],"flavorPreference":"甜"}
     */
    private void parsePreferences(MemberProfile p) {
        if (p.preferencesJson == null || p.preferencesJson.isBlank()) {
            p.hasValidPreferences = false;
            p.favoriteBreeds = Collections.emptyList();
            p.flavorPreference = "";
            return;
        }
        try {
            Map<String, Object> prefMap = objectMapper.readValue(p.preferencesJson, Map.class);
            // 提取 favoriteBreeds
            Object breedsObj = prefMap.get("favoriteBreeds");
            if (breedsObj instanceof List) {
                p.favoriteBreeds = new ArrayList<>();
                for (Object b : (List<?>) breedsObj) {
                    if (b != null) p.favoriteBreeds.add(b.toString());
                }
            } else {
                p.favoriteBreeds = Collections.emptyList();
            }
            // 提取 flavorPreference
            Object flavor = prefMap.get("flavorPreference");
            p.flavorPreference = flavor != null ? flavor.toString() : "";
            p.hasValidPreferences = !p.favoriteBreeds.isEmpty() || !p.flavorPreference.isEmpty();
        } catch (Exception e) {
            p.hasValidPreferences = false;
            p.favoriteBreeds = Collections.emptyList();
            p.flavorPreference = "";
        }
    }

    /**
     * 分析用户历史行为：判断新用户、计算近30天到店次数、是否沉睡用户
     * <p>
     * 沉睡用户修复说明（隐患2）：
     * 原逻辑中，若用户有预约记录但 member_ext.last_visit_time = null，
     * 会被直接标记为沉睡用户，导致误判。
     * 修复方案：lastVisitTime 为 null 时，从 reservations 记录中取最近一次
     * confirmed/completed 预约时间作为替代判断依据；若仍无法确定则视为正常用户（不标记沉睡）。
     */
    private void analyzeHistory(MemberProfile p) {
        // 通过 reservations 表统计
        var example = new cn.edu.bjfu.nekocafe.entity.ReservationsExample();
        example.createCriteria().andUserIdEqualTo(p.userId)
                .andStatusIn(Arrays.asList("confirmed", "completed"));
        List<cn.edu.bjfu.nekocafe.entity.Reservations> reservations =
                reservationsMapper.selectByExample(example);

        p.isNewUser = reservations.isEmpty();

        // 近30天到店次数（R14）
        LocalDate now = LocalDate.now();
        LocalDate thirtyDaysAgo = now.minusDays(30);
        int count30d = 0;
        java.util.Date latestReservationTs = null;  // 记录最近一次预约时间（用于沉睡判断兜底）
        for (var r : reservations) {
            if (r.getReservationTime() != null) {
                java.util.Date ts = r.getReservationTime();
                LocalDate resDate = ts.toInstant()
                        .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                if (!resDate.isBefore(thirtyDaysAgo)) count30d++;
                if (latestReservationTs == null || ts.after(latestReservationTs)) {
                    latestReservationTs = ts;
                }
            }
        }
        p.visitCount30d = count30d;

        // 沉睡用户判断（R15）：超60天未到店
        // 优先使用 member_ext.last_visit_time；若为 null，则用 reservations 最近预约时间兜底
        // 若两者均无法确定，则保守处理（不标记沉睡），避免误判活跃用户
        p.isSleepingUser = false;
        java.util.Date effectiveLastVisit = p.lastVisitTime;
        if (effectiveLastVisit == null && latestReservationTs != null) {
            effectiveLastVisit = new java.util.Date(latestReservationTs.getTime());
        }
        if (effectiveLastVisit != null) {
            long daysSinceLastVisit = ChronoUnit.DAYS.between(
                    effectiveLastVisit.toInstant()
                            .atZone(java.time.ZoneId.systemDefault()).toLocalDate(),
                    now);
            p.isSleepingUser = daysSinceLastVisit > 60;
        }
        // lastVisitTime 和 reservations 均无法提供时间时，保持 isSleepingUser = false
    }

    // ================================================================
    //  Step2: 猫咪推荐（R1 + R3 + R11）
    // ================================================================

    private List<Map<String, Object>> recommendCats(MemberProfile p, Boolean hasChild) {
        List<Map<String, Object>> results = new ArrayList<>();

        // 获取全部猫咪候选池
        var catExample = new cn.edu.bjfu.nekocafe.entity.CatProfilesExample();
        catExample.createCriteria();  // 全部
        List<cn.edu.bjfu.nekocafe.entity.CatProfiles> allCats =
                catProfilesMapper.selectByExample(catExample);

        if (allCats == null || allCats.isEmpty()) {
            return results;  // 空列表兜底
        }

        // ---- R1 判断: 有无有效偏好 ----
        if (!p.hasValidPreferences) {
            // R1 降级: 随机取 N 只猫咪
            Collections.shuffle(allCats);
            for (int i = 0; i < Math.min(TOP_CATS, allCats.size()); i++) {
                results.add(buildCatVO(allCats.get(i), 50));
            }
            return results;
        }

        // ---- R3: 品种匹配评分 ----
        for (var cat : allCats) {
            double matchScore = computeCatMatchScore(cat, p);
            if (matchScore > 0) {
                Map<String, Object> vo = buildCatVO(cat, matchScore);
                vo.put("_score", matchScore);  // 临时字段用于排序
                results.add(vo);
            }
        }

        // 如果品种匹配结果不足，补充随机猫咪
        if (results.size() < TOP_CATS) {
            Set<Integer> addedIds = new HashSet<>();
            for (var vo : results) addedIds.add((Integer) vo.get("catId"));
            for (var cat : allCats) {
                if (addedIds.contains(cat.getCatId())) continue;
                results.add(buildCatVO(cat, 40));  // 默认低分填充
                if (results.size() >= TOP_CATS) break;
            }
        }

        // ---- R11: 有小孩时筛选温顺猫（前置加分）----
        if (Boolean.TRUE.equals(hasChild)) {
            List<cn.edu.bjfu.nekocafe.entity.CatProfiles> gentleCats =
                    catProfilesMapper.selectByPersonality("温顺");
            Set<Integer> gentleIds = new HashSet<>();
            for (var gc : gentleCats) gentleIds.add(gc.getCatId());
            for (var vo : results) {
                if (gentleIds.contains(vo.get("catId"))) {
                    Double s = ((Number) vo.getOrDefault("_score", 0.0)).doubleValue();
                    vo.put("_score", s + 15);  // 温顺猫额外加分
                }
            }
        }

        // 按 score 降序排列
        results.sort((a, b) -> Double.compare(
                ((Number) b.getOrDefault("_score", 0.0)).doubleValue(),
                ((Number) a.getOrDefault("_score", 0.0)).doubleValue()));

        // 移除临时 _score 字段，截取 Top-N
        List<Map<String, Object>> finalCats = new ArrayList<>();
        for (int i = 0; i < Math.min(TOP_CATS, results.size()); i++) {
            Map<String, Object> vo = new HashMap<>(results.get(i));
            vo.remove("_score");
            finalCats.add(vo);
        }
        return finalCats;
    }

    /**
     * 计算单只猫咪的匹配得分
     * R3: breed ∈ favoriteBreeds → base=60, 每多一个匹配属性+10
     * <p>
     * 别名处理：用户偏好可能使用简称（如"英短"），数据库存全名（如"英国短毛猫"），
     * 通过 BREED_ALIAS_MAP 将简称展开后再进行双向包含匹配。
     */
    private double computeCatMatchScore(cn.edu.bjfu.nekocafe.entity.CatProfiles cat, MemberProfile p) {
        double score = 0;

        // R3: 品种匹配（核心）— 支持别名展开
        if (cat.getBreed() != null) {
            String catBreed = cat.getBreed();
            for (String fb : p.favoriteBreeds) {
                // 将用户简称展开为全名再做匹配
                String fbExpanded = BREED_ALIAS_MAP.getOrDefault(fb, fb);
                if (catBreed.contains(fb) || fb.contains(catBreed)
                        || catBreed.contains(fbExpanded) || fbExpanded.contains(catBreed)) {
                    score += 60;
                    break;
                }
            }
        }

        // 性格加分（通用）
        if (cat.getPersonality() != null) {
            String personality = cat.getPersonality();
            if (personality.contains("粘人")) score += 10;  // 猫咖特色
            if (personality.contains("活泼")) score += 8;
        }

        return score;
    }

    private Map<String, Object> buildCatVO(cn.edu.bjfu.nekocafe.entity.CatProfiles cat, double score) {
        Map<String, Object> vo = new LinkedHashMap<>();
        vo.put("catId", cat.getCatId());
        vo.put("name", cat.getName());
        vo.put("breed", cat.getBreed());
        vo.put("personality", cat.getPersonality());
        vo.put("avatarUrl", cat.getAvatarUrl());
        vo.put("matchScore", Math.min(100, (long) Math.round(score)));
        return vo;
    }

    // ================================================================
    //  Step3: 菜品推荐（R2 + R4 + R5 + R8 + R9 + R16 + R17）
    // ================================================================

    private List<Map<String, Object>> recommendDishes(MemberProfile p) {
        List<Map<String, Object>> candidates = new ArrayList<>();

        // ---- R4: 用户历史点餐频次 ----
        List<Map<String, Object>> userFreq = orderItemsMapper.selectDishFrequencyByUserId(p.userId);
        if (userFreq != null && !userFreq.isEmpty()) {
            for (Map<String, Object> row : userFreq) {
                candidates.add(buildDishCandidate(
                        ((Number) row.get("dishid")).intValue(),
                        "history",
                        ((Number) row.getOrDefault("totalquantity", 0)).doubleValue() * 10  // 频次加权
                ));
            }
        }

        // ---- R16: 同偏好协同过滤 ----
        if (p.flavorPreference != null && !p.flavorPreference.isEmpty()) {
            List<Map<String, Object>> collabDishes = orderItemsMapper.selectCollaborativeFilterDishes(
                    p.userId, p.flavorPreference, 5);
            if (collabDishes != null) {
                for (Map<String, Object> row : collabDishes) {
                    addOrUpdateDishCandidate(candidates,
                            ((Number) row.get("dishid")).intValue(),
                            "collaborative",
                            ((Number) row.getOrDefault("ordercount", 0)).doubleValue() * 8
                    );
                }
            }
        }

        // ---- R2: 口味标签匹配 ----
        if (p.flavorPreference != null && !p.flavorPreference.isEmpty()) {
            List<cn.edu.bjfu.nekocafe.entity.Dishes> tagMatches =
                    dishesMapper.selectByTagKeyword(p.flavorPreference);
            for (var d : tagMatches) {
                addOrUpdateDishCandidate(candidates, d.getDishId(), "tag_match", 25);
            }
        }

        // ---- R5: 新用户降级 / 结果不够时补充全店热门 ----
        if (candidates.size() < TOP_DISHES || p.isNewUser) {
            List<Map<String, Object>> hotDishes = dishesMapper.selectHotDishesByCategory(2);
            if (hotDishes != null) {
                for (Map<String, Object> row : hotDishes) {
                    addOrUpdateDishCandidate(candidates,
                            ((Number) row.get("dishid")).intValue(),
                            "hot_global",
                            ((Number) row.getOrDefault("ordercount", 0)).doubleValue() * 3
                    );
                }
            }
        }

        // ---- R8: 时段加权 ----
        String timeSlot = detectTimeSlot();
        for (var cand : candidates) {
            String reasonType = (String) cand.get("_reasonType");
            // 时段与 category 匹配时加分
            if ("lunch".equals(timeSlot) && isLightMealCategory((String) cand.get("category"))) {
                cand.put("_score", ((Number) cand.get("_score")).doubleValue() + 5);
            } else if ("afternoon_tea".equals(timeSlot) &&
                    ("甜品".equals(cand.get("category")) || "饮品".equals(cand.get("category")))) {
                cand.put("_score", ((Number) cand.get("_score")).doubleValue() + 8);
            } else if ("dinner".equals(timeSlot) && isMainDishCategory((String) cand.get("category"))) {
                cand.put("_score", ((Number) cand.get("_score")).doubleValue() + 5);
            }
        }

        // ---- R9: 季节性加权 ----
        Month currentMonth = LocalDate.now().getMonth();
        for (var cand : candidates) {
            String tags = (String) cand.get("tags");
            if (tags == null) continue;
            if (isSummer(currentMonth) && (tags.contains("冰") || tags.contains("冷"))) {
                cand.put("_score", ((Number) cand.get("_score")).doubleValue() + 6);
            }
            if (isWinter(currentMonth) && (tags.contains("热") || tags.contains("暖"))) {
                cand.put("_score", ((Number) cand.get("_score")).doubleValue() + 6);
            }
        }

        // ---- R14/R15: 会员分层调整 ----
        if (p.visitCount30d >= 3) {
            // R14: 复购用户 — 在结果中标记"新品"（如果有新品标签的菜）
            for (var cand : candidates) {
                String tags = (String) cand.get("tags");
                if (tags != null && tags.contains("新品")) {
                    cand.put("_score", ((Number) cand.get("_score")).doubleValue() + 12);
                    cand.put("_badge", "新品试吃");
                }
            }
        }
        if (p.isSleepingUser) {
            // R15: 沉睡用户 — 追加一条"限时优惠"
            Map<String, Object> promoItem = new LinkedHashMap<>();
            promoItem.put("dishId", -1);  // 特殊标识
            promoItem.put("name", "[限时] 回归特惠套餐");
            promoItem.put("category", "套餐");
            promoItem.put("price", 39.90);
            promoItem.put("_reasonType", "recall_promo");
            promoItem.put("_score", 70.0);
            candidates.add(promoItem);
        }

        // ---- 排序 & 截取 Top-N ----
        candidates.sort((a, b) -> Double.compare(
                ((Number) b.getOrDefault("_score", 0.0)).doubleValue(),
                ((Number) a.getOrDefault("_score", 0.0)).doubleValue()));

        List<Map<String, Object>> finalDishes = new ArrayList<>();
        for (int i = 0; i < Math.min(TOP_DISHES, candidates.size()); i++) {
            Map<String, Object> vo = cleanDishCandidate(new LinkedHashMap<>(candidates.get(i)));
            finalDishes.add(vo);
        }

        // ---- R17 兜底: 如果还是空的 ----
        if (finalDishes.isEmpty()) {
            finalDishes = fallbackGlobalDishes();
        }

        return finalDishes;
    }

    private Map<String, Object> buildDishCandidate(int dishId, String reasonType, double baseScore) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("dishId", dishId);
        m.put("_reasonType", reasonType);
        m.put("_score", baseScore);
        return m;
    }

    private void addOrUpdateDishCandidate(List<Map<String, Object>> list, int dishId,
                                           String reasonType, double bonusScore) {
        // 查找是否已存在
        for (var item : list) {
            if (((Number) item.get("dishId")).intValue() == dishId) {
                // 累加分数
                item.put("_score", ((Number) item.get("_score")).doubleValue() + bonusScore);
                // 合并原因类型
                String existing = (String) item.get("_reasonTypes");
                item.put("_reasonTypes", (existing == null ? "" : existing + ",") + reasonType);
                return;
            }
        }
        // 不存在则新增
        list.add(buildDishCandidate(dishId, reasonType, bonusScore));
    }

    /** 从数据库加载完整菜品信息并清理临时字段 */
    private Map<String, Object> cleanDishCandidate(Map<String, Object> cand) {
        int dishId = ((Number) cand.get("dishId")).intValue();

        // -1 是特殊促销项，不需要查数据库
        if (dishId < 0) {
            cand.remove("_score");
            cand.remove("_reasonType");
            cand.remove("_reasonTypes");
            return cand;
        }

        cn.edu.bjfu.nekocafe.entity.Dishes dish = dishesMapper.selectByPrimaryKey(dishId);
        if (dish == null) {
            cand.put("name", "(已下架)");
            cand.put("category", "未知");
            cand.put("price", 0);
        } else {
            cand.put("name", dish.getName());
            cand.put("category", dish.getCategory());
            cand.put("price", dish.getPrice());
            cand.put("imageUrl", dish.getImageUrl());
            cand.put("tags", dish.getTags());
        }

        Number rawScore = (Number) cand.remove("_score");
        cand.put("score", rawScore != null ? Math.min(100, Math.round(rawScore.doubleValue())) : 0);

        // 保留 badge
        Object badge = cand.remove("_badge");
        if (badge != null) cand.put("badge", badge);

        cand.remove("_reasonType");
        cand.remove("_reasonTypes");
        return cand;
    }

    /** R17: 全店销量兜底 */
    private List<Map<String, Object>> fallbackGlobalDishes() {
        List<Map<String, Object>> result = new ArrayList<>();
        List<Map<String, Object>> ranking = orderItemsMapper.selectGlobalDishRanking(5);
        if (ranking != null) {
            for (int i = 0; i < ranking.size(); i++) {
                Map<String, Object> row = ranking.get(i);
                int dishId = ((Number) row.get("dishid")).intValue();
                cn.edu.bjfu.nekocafe.entity.Dishes dish = dishesMapper.selectByPrimaryKey(dishId);
                Map<String, Object> vo = new LinkedHashMap<>();
                vo.put("dishId", dishId);
                vo.put("name", dish != null ? dish.getName() : "(未知)");
                vo.put("category", dish != null ? dish.getCategory() : "未知");
                vo.put("price", dish != null ? dish.getPrice() : 0);
                vo.put("score", 50 - i * 5);  // 递减分值
                vo.put("reason", "全店热销");
                result.add(vo);
            }
        }
        return result;
    }

    // ================================================================
    //  Step4: 桌位推荐（R7 + R10 + R13）
    // ================================================================

    private List<Map<String, Object>> recommendTables(MemberProfile p, Integer companionCount) {
        List<Map<String, Object>> tables = new ArrayList<>();

        int partySize = companionCount != null ? companionCount : 1;

        // ---- R7: 工作日 vs 周末 ----
        DayOfWeek dow = LocalDate.now().getDayOfWeek();
        boolean isWeekend = (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY);
        String periodLabel = isWeekend ? "周末聚会" : "工作日用餐";

        // ---- R13: VIP 优先包厢 ----
        boolean isVip = (p.level != null && p.level >= 3);

        // ---- R10: 根据人数匹配桌位类型 ----
        String seatType;
        if (partySize <= 1) {
            seatType = "单人座";
        } else if (partySize <= 3) {
            seatType = "双人标准座";
        } else {
            seatType = "多人聚坐";
        }

        try {
            // ---- 查询真实可用桌位 ----
            // R13: VIP 用户优先查询 vip 类型桌位
            if (isVip) {
                List<cn.edu.bjfu.nekocafe.entity.Tables> vipTables =
                        tablesMapper.selectAvailableTablesForRecommend(partySize, "vip", 1);
                for (var t : vipTables) {
                    tables.add(buildRealTableVO(t, "会员专属私密空间", 95));
                }
            }

            // 查询适合当前人数的普通桌位（不限类型，容量 ≥ partySize）
            int remaining = TOP_TABLES - tables.size();
            if (remaining > 0) {
                List<cn.edu.bjfu.nekocafe.entity.Tables> normalTables =
                        tablesMapper.selectAvailableTablesForRecommend(partySize, null, remaining + 2);
                // 过滤掉已加入的 VIP 桌位，按靠窗/安静角落顺序选取
                Set<Integer> addedIds = new HashSet<>();
                for (var vo : tables) addedIds.add((Integer) vo.get("tableId"));

                int scoreBase = 80;
                for (var t : normalTables) {
                    if (addedIds.contains(t.getTableId())) continue;
                    String reason = periodLabel + " · " + seatType +
                            (partySize > 3 ? " · 适合" + partySize + "人" : "");
                    tables.add(buildRealTableVO(t, reason, scoreBase));
                    addedIds.add(t.getTableId());
                    scoreBase -= 15;
                    if (tables.size() >= TOP_TABLES) break;
                }
            }
        } catch (Exception e) {
            // 数据库查询异常时降级为描述性信息（不使用负数 ID）
            tables.clear();
        }

        // ---- 降级兜底：数据库无数据或查询异常时给出描述性推荐 ----
        if (tables.isEmpty()) {
            if (isVip) {
                tables.add(buildFallbackTableVO("VIP 包厢", "vip", "会员专属私密空间", 95));
            }
            tables.add(buildFallbackTableVO(seatType + "（靠窗推荐）", seatType,
                    periodLabel + " · " + seatType + (partySize > 3 ? " · 适合" + partySize + "人" : ""), 80));
            if (tables.size() < TOP_TABLES) {
                tables.add(buildFallbackTableVO(seatType + "（安静角落）", seatType,
                        "安静区域 · 适合专注撸猫", 65));
            }
        }

        return tables;
    }

    /** 从真实 Tables 实体构建 VO */
    private Map<String, Object> buildRealTableVO(cn.edu.bjfu.nekocafe.entity.Tables t,
                                                  String reason, int score) {
        Map<String, Object> vo = new LinkedHashMap<>();
        vo.put("tableId", t.getTableId());
        vo.put("tableNo", t.getTableNo());
        vo.put("type", t.getTableType());
        vo.put("name", t.getTableType() + "（" + t.getTableNo() + "）");
        vo.put("capacity", t.getCapacity());
        vo.put("catTheme", t.getCatTheme());
        vo.put("recommendReason", reason);
        vo.put("score", score);
        return vo;
    }

    /** 降级兜底时构建描述性 VO（不含 tableId，前端显示时作为建议而非预约目标）*/
    private Map<String, Object> buildFallbackTableVO(String name, String type,
                                                      String reason, int score) {
        Map<String, Object> vo = new LinkedHashMap<>();
        vo.put("tableId", null);   // null 表示未绑定真实桌位，前端应提示"到店选座"
        vo.put("tableNo", null);
        vo.put("type", type);
        vo.put("name", name);
        vo.put("recommendReason", reason);
        vo.put("score", score);
        vo.put("fallback", true);  // 标记为降级结果
        return vo;
    }

    // ================================================================
    //  Step5: 后处理（R18 + R19）
    // ================================================================

    /** R18 互斥过滤: 已有辣味主菜时不重复推荐冰饮 */
    private List<Map<String, Object>> postProcessDishes(List<Map<String, Object>> dishes) {
        boolean hasSpicy = false;
        boolean hasSweet = false;
        boolean hasCoffee = false;

        for (var d : dishes) {
            String tags = (String) d.get("tags");
            if (tags != null) {
                if (tags.contains("辣")) hasSpicy = true;
                if (tags.contains("甜") || "甜品".equals(d.get("category"))) hasSweet = true;
                if (tags.contains("咖啡") || ("饮品".equals(d.get("category")) &&
                    d.get("name") != null && d.get("name").toString().contains("咖啡"))) hasCoffee = true;
            }
        }

        // R18 过滤互斥项
        Iterator<Map<String, Object>> it = dishes.iterator();
        while (it.hasNext()) {
            var d = it.next();
            String tags = (String) d.get("tags");

            // 辣 + 冰水 → 移除冰水类
            if (hasSpicy && tags != null &&
                    (tags.contains("冰水") || tags.contains("冰饮") || tags.contains("冷泡"))) {
                it.remove();
                continue;
            }
            // 已有甜点 → 移除第二份甜点（保留第一份）
            if (hasSweet && "甜品".equals(d.get("category"))) {
                hasSweet = false;  // 只移除第二份及之后的
                continue;
            }
        }

        // R19: 有咖啡时追加甜点（如果还没有甜点）
        if (hasCoffee) {
            boolean alreadyHasDessert = false;
            for (var d : dishes) {
                if ("甜品".equals(d.get("category"))) { alreadyHasDessert = true; break; }
            }
            if (!alreadyHasDessert) {
                // 使用专用方法查询第一道在售甜品，避免 setOrderByClause 注入 LIMIT 的非标准用法
                cn.edu.bjfu.nekocafe.entity.Dishes dessert =
                        dishesMapper.selectFirstActiveDishByCategory("甜品");
                if (dessert != null) {
                    Map<String, Object> pairVO = new LinkedHashMap<>();
                    pairVO.put("dishId", dessert.getDishId());
                    pairVO.put("name", dessert.getName());
                    pairVO.put("category", dessert.getCategory());
                    pairVO.put("price", dessert.getPrice());
                    pairVO.put("score", 55);
                    pairVO.put("reason", "搭配推荐");
                    dishes.add(pairVO);
                }
            }
        }

        return dishes;
    }

    // ================================================================
    //  工具方法
    // ================================================================

    /** R8: 检测当前时段 */
    private String detectTimeSlot() {
        LocalTime now = LocalTime.now();
        if (now.isAfter(LocalTime.of(11, 0)) && now.isBefore(LocalTime.of(14, 0))) {
            return "lunch";
        } else if (now.isAfter(LocalTime.of(14, 0)) && now.isBefore(LocalTime.of(17, 0))) {
            return "afternoon_tea";  // 猫咖重点时段
        } else if (now.isAfter(LocalTime.of(17, 0)) && now.isBefore(LocalTime.of(21, 0))) {
            return "dinner";
        }
        return "afternoon_tea";  // 默认
    }

    private boolean isLightMealCategory(String category) {
        return "轻食".equals(category) || "简餐".equals(category) || "套餐".equals(category);
    }

    private boolean isMainDishCategory(String category) {
        return "正餐".equals(category) || "主食".equals(category) || "招牌".equals(category);
    }

    private boolean isSummer(Month m) {
        return m == Month.JUNE || m == Month.JULY || m == Month.AUGUST ||
               m == Month.SEPTEMBER;
    }

    private boolean isWinter(Month m) {
        return m == Month.DECEMBER || m == Month.JANUARY || m == Month.FEBRUARY;
    }

    /** 构建推荐理由文本 */
    private String buildReason(MemberProfile p) {
        List<String> reasons = new ArrayList<>();

        if (p.isNewUser) {
            reasons.add("为您精选全店人气推荐");
        } else {
            if (p.hasValidPreferences) {
                if (!p.favoriteBreeds.isEmpty())
                    reasons.add("根据您喜欢的" + String.join("/", p.favoriteBreeds) + "猫咪");
                if (p.flavorPreference != null && !p.flavorPreference.isEmpty())
                    reasons.add("结合您偏好的" + p.flavorPreference + "口味");
            }
            reasons.add("参考您的用餐历史");
        }

        // 时间上下文
        DayOfWeek dow = LocalDate.now().getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
            reasons.add("周末放松时刻");
        }

        // 沉睡用户
        if (p.isSleepingUser) {
            reasons.add("欢迎回来！为您准备了专属回归礼遇");
        }

        return String.join("；", reasons);
    }

    /** 构建 userProfile VO */
    private Map<String, Object> buildUserprofileVO(MemberProfile p) {
        Map<String, Object> vo = new LinkedHashMap<>();
        vo.put("level", p.level);
        vo.put("preferences", p.hasValidPreferences ?
                Map.of("favoriteBreeds", p.favoriteBreeds, "flavorPreference", p.flavorPreference)
                : null);
        vo.put("isVip", p.level != null && p.level >= 3);
        vo.put("visitCount30d", p.visitCount30d);
        vo.put("isNewUser", p.isNewUser);
        vo.put("isSleepingUser", p.isSleepingUser);
        return vo;
    }

    /** 错误结果 */
    private Map<String, Object> buildErrorResult(String msg) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("reason", msg);
        r.put("cats", Collections.emptyList());
        r.put("dishes", Collections.emptyList());
        r.put("tables", Collections.emptyList());
        r.put("userProfile", null);
        return r;
    }

    /** 写入 Redis 缓存 */
    private void writeToCache(String cacheKey, Map<String, Object> result) {
        if (redisTemplate == null) return;
        try {
            String json = objectMapper.writeValueAsString(result);
            redisTemplate.opsForValue().set(cacheKey, json, CACHE_TTL_SECONDS, TimeUnit.SECONDS);
        } catch (Exception ignored) {
            // Redis 不可用时静默失败
        }
    }
}
