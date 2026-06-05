package cn.edu.bjfu.nekocafe.service.impl;

import cn.edu.bjfu.nekocafe.dto.PromotionCalcDTO;
import cn.edu.bjfu.nekocafe.mapper.PromotionsMapper;
import cn.edu.bjfu.nekocafe.mapper.UserCouponsMapper;
import cn.edu.bjfu.nekocafe.service.CouponService;
import cn.edu.bjfu.nekocafe.vo.CouponVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

/**
 * 优惠券 & 促销服务实现
 * 负责人：___
 *
 * 实现要点：
 *   listCoupons：查 user_coupons WHERE userId=? JOIN promotions 获取名称和规则
 *   listAvailableCoupons：在 listCoupons 基础上过滤 status=unused 且满足 minAmount 门槛
 *     额外计算 saving 字段
 *   getPromotionRules：查 promotions 表 isActive=true，解析 ruleJson 字段（JSON 格式）
 *   calculatePromotion：
 *     1. 先应用折扣券（discount 类型），再应用满减（cashback 类型）
 *     2. 遵守 stackingRules 中的叠加逻辑
 *     3. 组装 breakdown 列表，每条说明一个优惠项
 */
@Service
public class CouponServiceImpl implements CouponService {

    @Autowired
    private UserCouponsMapper userCouponsMapper;

    @Autowired
    private PromotionsMapper promotionsMapper;

    @Override
    public List<CouponVO> listCoupons(Long userId) {
        throw new UnsupportedOperationException("CouponServiceImpl.listCoupons 尚未实现");
    }

    @Override
    public List<CouponVO> listAvailableCoupons(Integer storeId, Integer amount, Long userId) {
        throw new UnsupportedOperationException("CouponServiceImpl.listAvailableCoupons 尚未实现");
    }

    @Override
    public Map<String, Object> getPromotionRules() {
        throw new UnsupportedOperationException("CouponServiceImpl.getPromotionRules 尚未实现");
    }

    @Override
    public Map<String, Object> calculatePromotion(Long userId, PromotionCalcDTO dto) {
        throw new UnsupportedOperationException("CouponServiceImpl.calculatePromotion 尚未实现");
    }
}
