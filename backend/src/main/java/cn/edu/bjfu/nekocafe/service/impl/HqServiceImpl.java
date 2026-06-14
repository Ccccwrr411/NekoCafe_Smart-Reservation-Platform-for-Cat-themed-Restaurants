package cn.edu.bjfu.nekocafe.service.impl;

import cn.edu.bjfu.nekocafe.dto.BatchSendCouponDTO;
import cn.edu.bjfu.nekocafe.dto.CreatePromotionDTO;
import cn.edu.bjfu.nekocafe.dto.SendCouponDTO;
import cn.edu.bjfu.nekocafe.entity.*;
import cn.edu.bjfu.nekocafe.mapper.*;
import cn.edu.bjfu.nekocafe.service.HqService;
import cn.edu.bjfu.nekocafe.vo.StoreCardVO;
import cn.edu.bjfu.nekocafe.vo.StoresOverviewVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 总部运营 Service 实现
 *
 * 涵盖：E-1 门店概览 / E-2 统计刷新 / E-3~E-7 活动CRUD / E-8~E-9 发券
 */
@Service
public class HqServiceImpl implements HqService {

    @Autowired private StoresMapper storesMapper;
    @Autowired private ReservationsMapper reservationsMapper;
    @Autowired private PaymentsMapper paymentsMapper;
    @Autowired private TablesMapper tablesMapper;
    @Autowired private UsersMapper usersMapper;
    @Autowired private ReviewsMapper reviewsMapper;
    @Autowired private MemberExtMapper memberExtMapper;
    @Autowired private PromotionsMapper promotionsMapper;
    @Autowired private UserCouponsMapper userCouponsMapper;
    @Autowired private UserRolesMapper userRolesMapper;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    // ==================== E-1 全部门店概览 ====================

    @Override
    public StoresOverviewVO getStoresOverview() {
        StoresOverviewVO vo = new StoresOverviewVO();

        StoresExample storeExample = new StoresExample();
        storeExample.createCriteria().andStatusEqualTo((short) 1);
        storeExample.setOrderByClause("store_id ASC");
        List<Stores> storeList = storesMapper.selectByExample(storeExample);

        List<StoreCardVO> cardList = new ArrayList<>();
        int totalRevenue = 0, totalOrders = 0;
        double totalTurnoverSum = 0.0;

        for (Stores store : storeList) {
            Integer storeId = store.getStoreId();
            StoreCardVO card = new StoreCardVO();
            card.setId(storeId);
            card.setName(store.getName());
            card.setStatus(store.getStatus() == 1 ? "open" : "closed");

            // 今日营收
            Long rev = paymentsMapper.sumTodayPaidByStoreId(storeId);
            int todayRevenue = rev == null ? 0 : rev.intValue();
            card.setTodayRevenue(todayRevenue);
            totalRevenue += todayRevenue;

            // 今日订单数
            Long oc = reservationsMapper.countTodayCompletedByStoreId(storeId);
            int todayOrders = oc == null ? 0 : oc.intValue();
            card.setTodayOrders(todayOrders);
            totalOrders += todayOrders;

            // 桌位统计
            Integer tc = tablesMapper.countActiveByStoreId(storeId);
            if (tc == null) tc = 0;
            card.setTableCount(tc);
            Integer occ = tablesMapper.countOccupiedByStoreId(storeId);
            if (occ == null) occ = 0;
            card.setAvailableTables(tc - occ);
            card.setOccupancyRate(tc > 0 ? occ.doubleValue() / tc.doubleValue() : 0.0);
            if (tc > 0) totalTurnoverSum += (double) todayOrders / tc;

            // 店长
            Map<String, Object> mgr = usersMapper.selectManagerByStoreId(storeId);
            card.setManager(mgr != null ? (String) mgr.get("real_name") : "未分配");
            card.setManagerPhone(mgr != null ? maskPhone((String) mgr.get("phone")) : "");

            // 评分
            Double rating = reviewsMapper.avgRatingByStoreId(storeId);
            card.setRating(rating == null ? 0.0 : Math.round(rating * 10.0) / 10.0);

            cardList.add(card);
        }

        vo.setStores(cardList);
        vo.setTotalRevenue(totalRevenue);
        vo.setTotalOrders(totalOrders);
        vo.setTotalMembers((int) memberExtMapper.countByExample(null));
        vo.setAvgDailyTurnover(cardList.isEmpty() ? 0.0
                : Math.round(totalTurnoverSum / cardList.size() * 100.0) / 100.0);
        return vo;
    }

    // ==================== E-2 每日统计写入（占位） ====================

    @Override
    public void refreshDailyStats(java.util.Date date) {
        throw new UnsupportedOperationException("refreshDailyStats 待后续实现");
    }

    // ==================== E-3 活动列表 ====================

    @Override
    public List<Promotions> listPromotions(String type, Boolean isActive, int page, int pageSize) {
        PromotionsExample ex = new PromotionsExample();
        PromotionsExample.Criteria c = ex.createCriteria();
        if (type != null && !type.isEmpty()) c.andTypeEqualTo(type);
        if (isActive != null) c.andIsActiveEqualTo(isActive);
        ex.setOrderByClause("end_time DESC, promo_id DESC");
        // MyBatis Example 不原生支持分页，用 RowBounds 在 XML 中指定
        // 这里采用最简单的：selectByExample 后手动截取（数据量不大时可行）
        List<Promotions> all = promotionsMapper.selectByExample(ex);
        int from = (page - 1) * pageSize;
        if (from >= all.size()) return Collections.emptyList();
        int to = Math.min(from + pageSize, all.size());
        return all.subList(from, to);
    }

    @Override
    public long countPromotions(String type, Boolean isActive) {
        PromotionsExample ex = new PromotionsExample();
        PromotionsExample.Criteria c = ex.createCriteria();
        if (type != null && !type.isEmpty()) c.andTypeEqualTo(type);
        if (isActive != null) c.andIsActiveEqualTo(isActive);
        return promotionsMapper.countByExample(ex);
    }

    // ==================== E-4 创建活动 ====================

    @Override
    public Promotions createPromotion(CreatePromotionDTO dto) {
        validatePromotionDTO(dto, true);

        Promotions p = new Promotions();
        p.setName(dto.getName());
        p.setType(dto.getType());
        try { p.setRuleJson(MAPPER.writeValueAsString(dto.getRuleJson())); } catch (Exception e) { p.setRuleJson("{}"); }
        p.setStartTime(dto.getStartTime());
        p.setEndTime(dto.getEndTime());
        p.setApplicableStores(dto.getApplicableStores());
        p.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);

        promotionsMapper.insertSelective(p);
        return p;
    }

    // ==================== E-5 编辑活动 ====================

    @Override
    public Promotions updatePromotion(Integer promoId, CreatePromotionDTO dto) {
        Promotions existing = promotionsMapper.selectByPrimaryKey(promoId);
        if (existing == null) throw new RuntimeException("活动不存在");

        // 活动已开始 → 不允许修改 type 和 rule_json
        Date now = new Date();
        boolean started = existing.getStartTime() != null && existing.getStartTime().before(now);

        if (started) {
            if (dto.getType() != null && !dto.getType().equals(existing.getType()))
                throw new RuntimeException("活动已开始，不能修改类型");
            if (dto.getRuleJson() != null) {
                // 允许修改一些非核心规则参数（如 max_discount），但核心字段不变
                // 为安全起见，已开始的活动禁止修改 rule_json
                throw new RuntimeException("活动已开始，不能修改规则");
            }
        } else {
            // 活动未开始 → 正常校验
            validatePromotionDTO(dto, false);
        }

        if (dto.getName() != null) existing.setName(dto.getName());
        if (dto.getType() != null) existing.setType(dto.getType());
        if (dto.getRuleJson() != null) try { existing.setRuleJson(MAPPER.writeValueAsString(dto.getRuleJson())); } catch (Exception e) { existing.setRuleJson("{}"); }
        if (dto.getStartTime() != null) existing.setStartTime(dto.getStartTime());
        if (dto.getEndTime() != null) existing.setEndTime(dto.getEndTime());
        if (dto.getApplicableStores() != null) existing.setApplicableStores(dto.getApplicableStores());
        if (dto.getIsActive() != null) existing.setIsActive(dto.getIsActive());

        promotionsMapper.updateByPrimaryKeySelective(existing);
        return existing;
    }

    // ==================== E-6 启用/停用 ====================

    @Override
    public void togglePromotion(Integer promoId) {
        Promotions p = promotionsMapper.selectByPrimaryKey(promoId);
        if (p == null) throw new RuntimeException("活动不存在");
        p.setIsActive(!Boolean.TRUE.equals(p.getIsActive()));
        promotionsMapper.updateByPrimaryKeySelective(p);
    }

    // ==================== E-7 删除活动 ====================

    @Override
    public void deletePromotion(Integer promoId) {
        Promotions p = promotionsMapper.selectByPrimaryKey(promoId);
        if (p == null) throw new RuntimeException("活动不存在");

        // 检查是否已有用户领取
        UserCouponsExample ex = new UserCouponsExample();
        ex.createCriteria().andPromoIdEqualTo(promoId);
        long claimed = userCouponsMapper.countByExample(ex);

        if (claimed > 0) {
            // 软删除：停用
            p.setIsActive(false);
            promotionsMapper.updateByPrimaryKeySelective(p);
            throw new RuntimeException("该活动已有 " + claimed + " 位用户领取，已自动停用（不可物理删除）");
        }
        promotionsMapper.deleteByPrimaryKey(promoId);
    }

    // ==================== E-8 单用户发券 ====================

    @Override
    public Map<String, Object> sendCoupon(SendCouponDTO dto) {
        Promotions promo = promotionsMapper.selectByPrimaryKey(dto.getPromoId());
        if (promo == null) throw new RuntimeException("活动不存在");
        if (!Boolean.TRUE.equals(promo.getIsActive())) throw new RuntimeException("活动已停用");

        // 检查是否已领取
        UserCouponsExample ex = new UserCouponsExample();
        ex.createCriteria().andUserIdEqualTo(dto.getUserId()).andPromoIdEqualTo(dto.getPromoId());
        List<UserCoupons> existing = userCouponsMapper.selectByExample(ex);
        if (!existing.isEmpty()) {
            // 已领取但未使用 → 不重复发放
            boolean hasUnused = existing.stream().anyMatch(c -> "UNUSED".equals(c.getStatus()));
            if (hasUnused) throw new RuntimeException("该用户已领取此活动优惠券，且尚未使用");
        }

        UserCoupons uc = new UserCoupons();
        uc.setUserId(dto.getUserId());
        uc.setPromoId(dto.getPromoId());
        uc.setStatus("UNUSED");
        uc.setCreatedAt(new Date());
        // 过期时间：优先用 dto 指定，否则用活动结束时间
        uc.setExpireTime(dto.getExpireTime() != null ? dto.getExpireTime() : promo.getEndTime());

        userCouponsMapper.insertSelective(uc);

        Map<String, Object> result = new HashMap<>();
        result.put("couponId", uc.getCouponId());
        result.put("promoName", promo.getName());
        result.put("success", true);
        return result;
    }

    // ==================== E-9 批量发券 ====================

    @Override
    public Map<String, Object> batchSendCoupon(BatchSendCouponDTO dto) {
        Promotions promo = promotionsMapper.selectByPrimaryKey(dto.getPromoId());
        if (promo == null) throw new RuntimeException("活动不存在");
        if (!Boolean.TRUE.equals(promo.getIsActive())) throw new RuntimeException("活动已停用");

        // 确定目标用户
        List<Long> targetUserIds;
        switch (dto.getTargetType()) {
            case "user":
                targetUserIds = dto.getUsersIds();
                break;
            case "role":
                targetUserIds = getUserIdsByRole(Integer.parseInt(dto.getTargetValue()));
                break;
            case "member_level":
                targetUserIds = getUserIdsByMemberLevel(resolveMemberLevel(dto.getTargetValue()));
                break;
            case "all":
                targetUserIds = getAllActiveUserIds();
                break;
            default:
                throw new RuntimeException("不支持的 targetType: " + dto.getTargetType());
        }

        if (targetUserIds == null || targetUserIds.isEmpty()) {
            throw new RuntimeException("没有找到目标用户");
        }

        Date expireTime = dto.getExpireTime() != null ? dto.getExpireTime() : promo.getEndTime();
        int success = 0, skipped = 0;

        for (Long uid : targetUserIds) {
            // 检查重复
            UserCouponsExample ex = new UserCouponsExample();
            ex.createCriteria().andUserIdEqualTo(uid).andPromoIdEqualTo(dto.getPromoId());
            List<UserCoupons> existing = userCouponsMapper.selectByExample(ex);
            if (existing.stream().anyMatch(c -> "UNUSED".equals(c.getStatus()))) {
                skipped++;
                continue;
            }

            UserCoupons uc = new UserCoupons();
            uc.setUserId(uid);
            uc.setPromoId(dto.getPromoId());
            uc.setStatus("UNUSED");
            uc.setExpireTime(expireTime);
            uc.setCreatedAt(new Date());
            userCouponsMapper.insertSelective(uc);
            success++;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("skipped", skipped);
        result.put("total", targetUserIds.size());
        result.put("promoName", promo.getName());
        return result;
    }

    // ==================== 辅助方法 ====================

    /**
     * 校验 rule_json 合法性
     */
    @SuppressWarnings("unchecked")
    private void validatePromotionDTO(CreatePromotionDTO dto, boolean isCreate) {
        if (dto.getName() == null || dto.getName().trim().isEmpty())
            throw new RuntimeException("活动名称不能为空");
        if (dto.getType() == null || (!"DISCOUNT".equals(dto.getType()) && !"VOUCHER".equals(dto.getType())))
            throw new RuntimeException("活动类型只能为 DISCOUNT 或 VOUCHER");

        Map<String, Object> rule = dto.getRuleJson();
        if (rule == null) throw new RuntimeException("ruleJson 不能为空");

        if ("DISCOUNT".equals(dto.getType())) {
            Object discount = rule.get("discount");
            if (discount == null)
                throw new RuntimeException("折扣券必须包含 discount 字段");
            double d = ((Number) discount).doubleValue();
            if (d <= 0 || d > 1)
                throw new RuntimeException("discount 必须在 0~1 之间（如 0.8 表示8折）");
            if (rule.get("max_discount") != null && ((Number) rule.get("max_discount")).doubleValue() < 0)
                throw new RuntimeException("max_discount 不能为负数");
            if (rule.get("min_spend") != null && ((Number) rule.get("min_spend")).doubleValue() < 0)
                throw new RuntimeException("min_spend 不能为负数");
        }

        if ("VOUCHER".equals(dto.getType())) {
            Object reduction = rule.get("reduction");
            if (reduction == null)
                throw new RuntimeException("满减券必须包含 reduction 字段");
            double r = ((Number) reduction).doubleValue();
            if (r <= 0)
                throw new RuntimeException("reduction 必须大于0");
            if (rule.get("min_spend") != null && ((Number) rule.get("min_spend")).doubleValue() < 0)
                throw new RuntimeException("min_spend 不能为负数");
        }

        if (dto.getStartTime() != null && dto.getEndTime() != null) {
            if (!dto.getEndTime().after(dto.getStartTime()))
                throw new RuntimeException("结束时间必须晚于开始时间");
        }
    }

    private List<Long> getUserIdsByRole(int roleId) {
        UserRolesExample ex = new UserRolesExample();
        ex.createCriteria().andRoleIdEqualTo(roleId);
        List<UserRoles> list = userRolesMapper.selectByExample(ex);
        List<Long> ids = new ArrayList<>();
        for (UserRoles ur : list) ids.add(ur.getUserId());
        return ids;
    }

    private int resolveMemberLevel(String name) {
        if (name == null) return 1;
        switch (name) {
            case "钻石会员": return 4;
            case "金卡会员": return 3;
            case "银卡会员": return 2;
            default: return 1; // "普通会员" 及其他
        }
    }

    private List<Long> getUserIdsByMemberLevel(int minLevel) {
        MemberExtExample ex = new MemberExtExample();
        ex.createCriteria().andLevelGreaterThanOrEqualTo(minLevel);
        List<MemberExt> list = memberExtMapper.selectByExample(ex);
        List<Long> ids = new ArrayList<>();
        for (MemberExt me : list) ids.add(me.getUserId());
        return ids;
    }

    private List<Long> getAllActiveUserIds() {
        UsersExample ex = new UsersExample();
        ex.createCriteria().andStatusEqualTo((short) 1);
        List<Users> list = usersMapper.selectByExample(ex);
        List<Long> ids = new ArrayList<>();
        for (Users u : list) ids.add(u.getUserId());
        return ids;
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 8) return phone;
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
