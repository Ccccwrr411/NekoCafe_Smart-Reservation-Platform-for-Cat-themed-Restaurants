package cn.edu.bjfu.nekocafe.service;

import cn.edu.bjfu.nekocafe.dto.BatchSendCouponDTO;
import cn.edu.bjfu.nekocafe.dto.CreatePromotionDTO;
import cn.edu.bjfu.nekocafe.dto.SendCouponDTO;
import cn.edu.bjfu.nekocafe.entity.Promotions;
import cn.edu.bjfu.nekocafe.vo.StoresOverviewVO;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 总部运营 Service 接口
 *
 * 接口清单：
 * E-1  GET  /api/hq/stores-overview   全部门店概览
 * E-2  POST /api/hq/stats/refresh     手动触发每日统计写入
 * E-3  GET  /api/hq/promotions       活动列表（分页）
 * E-4  POST /api/hq/promotions       创建活动
 * E-5  PUT  /api/hq/promotions/{id}  编辑活动
 * E-6  PATCH /api/hq/promotions/{id}/toggle  启用/停用活动
 * E-7  DELETE /api/hq/promotions/{id}  删除活动（软删除）
 * E-8  POST /api/hq/coupons/send       给指定用户发券
 * E-9  POST /api/hq/coupons/batch-send  批量发券
 */
public interface HqService {

    // ========== E-1 全部门店概览 ==========
    StoresOverviewVO getStoresOverview();

    // ========== E-2 每日统计写入 ==========
    void refreshDailyStats(Date date);

    // ========== E-3 ~ E-7 活动管理 ==========

    /** E-3 分页查询活动列表，支持按 type/status 筛选 */
    List<Promotions> listPromotions(String type, Boolean isActive, int page, int pageSize);

    /** E-3 查询活动总数 */
    long countPromotions(String type, Boolean isActive);

    /** E-4 创建活动（含 rule_json 校验） */
    Promotions createPromotion(CreatePromotionDTO dto);

    /** E-5 编辑活动 */
    Promotions updatePromotion(Integer promoId, CreatePromotionDTO dto);

    /** E-6 启用/停用活动 */
    void togglePromotion(Integer promoId);

    /** E-7 删除活动（有已领取记录则软删除） */
    void deletePromotion(Integer promoId);

    // ========== E-8 ~ E-9 发券 ==========

    /** E-8 给指定用户发一张券 */
    Map<String, Object> sendCoupon(SendCouponDTO dto);

    /** E-9 批量发券 */
    Map<String, Object> batchSendCoupon(BatchSendCouponDTO dto);
}
