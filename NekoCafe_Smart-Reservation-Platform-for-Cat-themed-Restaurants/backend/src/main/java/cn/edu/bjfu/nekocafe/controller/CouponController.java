package cn.edu.bjfu.nekocafe.controller;

import cn.edu.bjfu.nekocafe.common.Result;
import cn.edu.bjfu.nekocafe.dto.PromotionCalcDTO;
import cn.edu.bjfu.nekocafe.service.CouponService;
import cn.edu.bjfu.nekocafe.vo.CouponVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * 优惠券 & 促销 Controller
 * 负责人：___
 * 接口：G-1 ~ G-4
 */
@RestController
@RequestMapping("/api")
public class CouponController {

    @Autowired
    private CouponService couponService;

    /** G-1 我的优惠券 */
    @GetMapping("/coupons")
    public Result<List<CouponVO>> listCoupons(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(couponService.listCoupons(userId));
    }

    /** G-2 下单可用优惠券 */
    @GetMapping("/coupons/available")
    public Result<List<CouponVO>> listAvailableCoupons(@RequestParam Integer storeId,
                                                        @RequestParam Integer amount,
                                                        HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(couponService.listAvailableCoupons(storeId, amount, userId));
    }

    /** G-3 促销活动规则 */
    @GetMapping("/promotions/rules")
    public Result<Map<String, Object>> getPromotionRules() {
        return Result.success(couponService.getPromotionRules());
    }

    /** G-4 优惠试算 */
    @PostMapping("/promotions/calculate")
    public Result<Map<String, Object>> calculatePromotion(@RequestBody PromotionCalcDTO dto,
                                                           HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(couponService.calculatePromotion(userId, dto));
    }
}
