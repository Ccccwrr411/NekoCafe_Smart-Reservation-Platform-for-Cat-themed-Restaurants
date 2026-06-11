package cn.edu.bjfu.nekocafe.service.impl;

import cn.edu.bjfu.nekocafe.dto.PromotionCalcDTO;
import cn.edu.bjfu.nekocafe.entity.Promotions;
import cn.edu.bjfu.nekocafe.entity.PromotionsExample;
import cn.edu.bjfu.nekocafe.entity.UserCoupons;
import cn.edu.bjfu.nekocafe.entity.UserCouponsExample;
import cn.edu.bjfu.nekocafe.mapper.PromotionsMapper;
import cn.edu.bjfu.nekocafe.mapper.UserCouponsMapper;
import cn.edu.bjfu.nekocafe.service.CouponService;
import cn.edu.bjfu.nekocafe.vo.CouponVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 优惠券 & 促销服务实现
 * 负责人：D 同学
 *
 * G-1: listCoupons          — 查 user_coupons + 联查 promotions 获取规则详情
 * G-2: listAvailableCoupons — 在 G-1 基础上过滤 status=unused + 满足 minAmount 门槛，计算 saving
 * G-3: getPromotionRules    — 查所有 isActive=true 的促销活动，返回规则列表 + 叠加策略
 * G-4: calculatePromotion   — 根据用户选中的优惠券计算最终优惠金额和 breakdown
 */
@Service
public class CouponServiceImpl implements CouponService {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private UserCouponsMapper userCouponsMapper;

    @Autowired
    private PromotionsMapper promotionsMapper;

    // ==================== G-1 : 我的优惠券 ====================

    @Override
    public List<CouponVO> listCoupons(Long userId) {
        // 1. 查用户持有的优惠券
        UserCouponsExample uce = new UserCouponsExample();
        uce.createCriteria().andUserIdEqualTo(userId);
        List<UserCoupons> userCoupons = userCouponsMapper.selectByExample(uce);

        // 2. 逐条联查 promotions 并组装 VO
        List<CouponVO> result = new ArrayList<>();
        for (UserCoupons uc : userCoupons) {
            Promotions promo = promotionsMapper.selectByPrimaryKey(uc.getPromoId());
            result.add(buildCouponVO(uc, promo));
        }
        return result;
    }

    // ==================== G-2 : 下单可用优惠券 ====================

    @Override
    public List<CouponVO> listAvailableCoupons(Integer storeId, Integer amount, Long userId) {
        // 先拉全部用户券，再内存过滤（数据量小，N+1 查询可接受）
        List<CouponVO> all = listCoupons(userId);

        return all.stream()
                // 状态必须是 unused
                .filter(c -> "unused".equals(c.getStatus()))
                // 满足最低消费门槛
                .filter(c -> c.getMinAmount() == null || c.getMinAmount() <= amount)
                // 计算每张券本次下单预计节省金额
                .peek(c -> c.setSaving(calcSaving(c, amount)))
                .collect(Collectors.toList());
    }

    // ==================== G-3 : 促销活动规则 ====================

    @Override
    public Map<String, Object> getPromotionRules() {
        // 1. 查询所有生效中的促销活动
        PromotionsExample example = new PromotionsExample();
        example.createCriteria().andIsActiveEqualTo(true);
        List<Promotions> promos = promotionsMapper.selectByExample(example);

        // 2. 组装 activePromotions
        List<Map<String, Object>> activeList = new ArrayList<>();
        for (Promotions p : promos) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("ruleId", String.valueOf(p.getPromoId()));
            item.put("name", p.getName());
            item.put("type", p.getType());
            item.put("ruleJson", parseRuleJson(p.getRuleJson()));
            item.put("startTime", p.getStartTime());
            item.put("endTime", p.getEndTime());
            activeList.add(item);
        }

        // 3. 叠加规则（课设版写死）
        Map<String, Object> stackingRules = new LinkedHashMap<>();
        stackingRules.put("maxStackCount", 3);
        stackingRules.put("rules", List.of(
                "同类型优惠券不可叠加使用",
                "折扣券(discount)优先于满减券(cashback)计算",
                "满减券取优惠力度最大的一张"
        ));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("activePromotions", activeList);
        result.put("stackingRules", stackingRules);
        return result;
    }

    // ==================== G-4 : 优惠试算 ====================

    @Override
    public Map<String, Object> calculatePromotion(Long userId, PromotionCalcDTO dto) {
        int originalAmount = dto.getAmount();
        List<String> selectedIds = dto.getCouponIds();

        // 拉用户所有优惠券，按 id 索引
        List<CouponVO> allCoupons = listCoupons(userId);

        int totalDiscount = 0;
        List<Map<String, Object>> breakdown = new ArrayList<>();

        // 先处理折扣券，再处理满减券（遵守叠加规则）
        // 折扣券
        for (String cid : selectedIds) {
            CouponVO c = findCoupon(allCoupons, cid);
            if (c != null && "discount".equals(c.getType()) && "unused".equals(c.getStatus())) {
                int saving = calcSaving(c, originalAmount - totalDiscount);
                totalDiscount += saving;
                breakdown.add(buildBreakdownItem(c, saving));
            }
        }

        // 满减券取最优一张
        int bestCashback = 0;
        CouponVO bestCashbackCoupon = null;
        for (String cid : selectedIds) {
            CouponVO c = findCoupon(allCoupons, cid);
            if (c != null && "cashback".equals(c.getType()) && "unused".equals(c.getStatus())) {
                int saving = calcSaving(c, originalAmount - totalDiscount);
                if (saving > bestCashback) {
                    bestCashback = saving;
                    bestCashbackCoupon = c;
                }
            }
        }
        if (bestCashbackCoupon != null) {
            totalDiscount += bestCashback;
            breakdown.add(buildBreakdownItem(bestCashbackCoupon, bestCashback));
        }

        int finalAmount = Math.max(0, originalAmount - totalDiscount);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("originalAmount", originalAmount);
        result.put("totalDiscount", totalDiscount);
        result.put("finalAmount", finalAmount);
        result.put("breakdown", breakdown);
        return result;
    }

    // ==================== 私有辅助方法 ====================

    /** 从 UserCoupons + Promotions 组装 CouponVO */
    private CouponVO buildCouponVO(UserCoupons uc, Promotions promo) {
        CouponVO vo = new CouponVO();
        vo.setId(String.valueOf(uc.getCouponId()));
        vo.setStatus(uc.getStatus());

        if (uc.getExpireTime() != null) {
            vo.setExpireDate(DATE_FMT.format(uc.getExpireTime()));
        }

        if (promo != null) {
            vo.setName(promo.getName());
            vo.setType(promo.getType());
            vo.setRuleId(String.valueOf(promo.getPromoId()));

            // 解析 ruleJson 字段提取 value / maxDiscount / minAmount / stackable
            Map<String, Object> rule = parseRuleJson(promo.getRuleJson());
            if (rule != null) {
                if (rule.get("value") instanceof Number) {
                    vo.setValue(((Number) rule.get("value")).doubleValue());
                }
                if (rule.get("maxDiscount") instanceof Number) {
                    vo.setMaxDiscount(((Number) rule.get("maxDiscount")).intValue());
                }
                if (rule.get("minAmount") instanceof Number) {
                    vo.setMinAmount(((Number) rule.get("minAmount")).intValue());
                }
                if (rule.get("stackable") instanceof Boolean) {
                    vo.setStackable((Boolean) rule.get("stackable"));
                }
            }
        }
        return vo;
    }

    /** 计算一张优惠券在给定订单金额下预计节省金额 */
    private int calcSaving(CouponVO vo, int orderAmount) {
        if (vo.getValue() == null) return 0;

        switch (vo.getType()) {
            case "discount":
                // value 为折扣率，如 0.85 表示 85 折
                int saving = (int) Math.round(orderAmount * (1 - vo.getValue()));
                if (vo.getMaxDiscount() != null && saving > vo.getMaxDiscount()) {
                    saving = vo.getMaxDiscount();
                }
                return saving;

            case "cashback":
                // value 为固定减免金额
                int cashback = vo.getValue().intValue();
                return Math.min(cashback, orderAmount); // 不能超过订单金额

            default:
                return 0;
        }
    }

    /** 在列表中按 id 查找优惠券 */
    private CouponVO findCoupon(List<CouponVO> list, String id) {
        return list.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst().orElse(null);
    }

    /** 构建优惠试算 breakdown 项 */
    private Map<String, Object> buildBreakdownItem(CouponVO c, int saving) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("couponId", c.getId());
        item.put("couponName", c.getName());
        item.put("type", c.getType());
        item.put("discountAmount", saving);
        return item;
    }

    /**
     * 安全解析 ruleJson 字段。
     * PostgreSQL 中 JSON/JSONB 列通过 JDBC 返回的是 PGobject，
     * toString() 即 JSON 字符串；如果已反序列化为 Map 则直接返回。
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseRuleJson(Object raw) {
        if (raw == null) return null;
        try {
            if (raw instanceof Map) return (Map<String, Object>) raw;
            return MAPPER.readValue(raw.toString(), new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return null;
        }
    }
}
