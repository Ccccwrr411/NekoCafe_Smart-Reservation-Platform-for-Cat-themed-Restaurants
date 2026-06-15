package cn.edu.bjfu.nekocafe.service;

import cn.edu.bjfu.nekocafe.dto.PromotionCalcDTO;
import cn.edu.bjfu.nekocafe.vo.CouponVO;
import java.util.List;
import java.util.Map;

/**
 * 优惠券 & 促销服务接口
 * 实现类：CouponServiceImpl
 */
public interface CouponService {

    /**
     * 获取用户的优惠券列表（G-1）
     */
    List<CouponVO> listCoupons(Long userId);

    /**
     * 获取下单可用的优惠券（G-2）
     * 额外计算每张券的预估节省金额（saving 字段）
     */
    List<CouponVO> listAvailableCoupons(Integer storeId, Integer amount, Long userId);

    /**
     * 获取当前所有促销活动规则（G-3）
     * 返回 activePromotions + stackingRules
     */
    Map<String, Object> getPromotionRules();

    /**
     * 优惠试算（G-4）
     * 返回 originalAmount + appliedPromotions + totalDiscount + finalAmount + breakdown
     */
    Map<String, Object> calculatePromotion(Long userId, PromotionCalcDTO dto);
}
