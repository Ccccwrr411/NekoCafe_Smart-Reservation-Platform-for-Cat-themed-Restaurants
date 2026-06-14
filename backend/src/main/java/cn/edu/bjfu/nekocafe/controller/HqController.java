package cn.edu.bjfu.nekocafe.controller;

import cn.edu.bjfu.nekocafe.annotation.OperationLog;
import cn.edu.bjfu.nekocafe.annotation.RequireRole;
import cn.edu.bjfu.nekocafe.common.Result;
import cn.edu.bjfu.nekocafe.dto.BatchSendCouponDTO;
import cn.edu.bjfu.nekocafe.dto.CreatePromotionDTO;
import cn.edu.bjfu.nekocafe.dto.SendCouponDTO;
import cn.edu.bjfu.nekocafe.entity.Promotions;
import cn.edu.bjfu.nekocafe.service.HqService;
import cn.edu.bjfu.nekocafe.vo.StoresOverviewVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 总部运营 Controller（仅限总部运营 roleId=4）
 *
 * 接口清单：
 * E-1  GET  /api/hq/stores-overview   全部门店概览（dashboard 前端调用）
 * E-2  POST /api/hq/stats/refresh     手动触发每日统计写入
 * E-3  GET  /api/hq/promotions       活动列表（分页）
 * E-4  POST /api/hq/promotions       创建活动
 * E-5  PUT  /api/hq/promotions/{id}  编辑活动
 * E-6  PATCH /api/hq/promotions/{id}/toggle  启用/停用
 * E-7  DELETE /api/hq/promotions/{id}  删除（软删除）
 * E-8  POST /api/hq/coupons/send       给指定用户发券
 * E-9  POST /api/hq/coupons/batch-send  批量发券
 */
@RestController
@RequestMapping("/api/hq")
@RequireRole(4)  // 仅限总部运营
public class HqController {

    @Autowired
    private HqService hqService;

    // ════════════════ E-1 全部门店概览 ════════════════

    @GetMapping("/stores-overview")
    public Result<StoresOverviewVO> getStoresOverview() {
        return Result.success(hqService.getStoresOverview());
    }

    // ════════════════ E-2 统计刷新（占位） ════════════════

    @OperationLog("触发每日统计刷新")
    @PostMapping("/stats/refresh")
    public Result<String> refreshDailyStats() {
        hqService.refreshDailyStats(null);
        return Result.success("每日统计刷新已触发");
    }

    // ════════════════ E-3 活动列表 ════════════════

    @GetMapping("/promotions")
    public Result<Map<String, Object>> listPromotions(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {

        List<Promotions> list = hqService.listPromotions(type, isActive, page, pageSize);
        long total = hqService.countPromotions(type, isActive);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return Result.success(result);
    }

    // ════════════════ E-4 创建活动 ════════════════

    @OperationLog(value = "创建优惠活动", recordParams = true)
    @PostMapping("/promotions")
    public Result<Promotions> createPromotion(@RequestBody CreatePromotionDTO dto) {
        return Result.success(hqService.createPromotion(dto));
    }

    // ════════════════ E-5 编辑活动 ════════════════

    @OperationLog("编辑优惠活动")
    @PutMapping("/promotions/{promoId}")
    public Result<Promotions> updatePromotion(
            @PathVariable Integer promoId,
            @RequestBody CreatePromotionDTO dto) {
        return Result.success(hqService.updatePromotion(promoId, dto));
    }

    // ════════════════ E-6 启用/停用 ════════════════

    @OperationLog("启用/停用优惠活动")
    @PatchMapping("/promotions/{promoId}/toggle")
    public Result<String> togglePromotion(@PathVariable Integer promoId) {
        hqService.togglePromotion(promoId);
        return Result.success("ok");
    }

    // ════════════════ E-7 删除活动 ════════════════

    @OperationLog("删除优惠活动")
    @DeleteMapping("/promotions/{promoId}")
    public Result<String> deletePromotion(@PathVariable Integer promoId) {
        hqService.deletePromotion(promoId);
        return Result.success("ok");
    }

    // ════════════════ E-8 给指定用户发券 ════════════════

    @OperationLog("给指定用户发券")
    @PostMapping("/coupons/send")
    public Result<Map<String, Object>> sendCoupon(@RequestBody SendCouponDTO dto) {
        return Result.success(hqService.sendCoupon(dto));
    }

    // ════════════════ E-9 批量发券 ════════════════

    @OperationLog(value = "批量发券", recordParams = true)
    @PostMapping("/coupons/batch-send")
    public Result<Map<String, Object>> batchSendCoupon(@RequestBody BatchSendCouponDTO dto) {
        return Result.success(hqService.batchSendCoupon(dto));
    }
}
