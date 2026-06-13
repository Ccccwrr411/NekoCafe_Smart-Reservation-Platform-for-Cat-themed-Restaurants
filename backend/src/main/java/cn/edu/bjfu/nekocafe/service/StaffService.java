package cn.edu.bjfu.nekocafe.service;

import cn.edu.bjfu.nekocafe.vo.DashboardMetricsVO;
import cn.edu.bjfu.nekocafe.vo.TableVO;
import java.util.List;
import java.util.Map;

/**
 * 店员 & 数据看板服务接口
 * 实现类：StaffServiceImpl
 */
public interface StaffService {

    /**
     * 获取运营指标数据（K-1）
     *
     * @param storeId 门店 ID
     * @param range   时间范围：7d / 30d / 90d
     */
    DashboardMetricsVO getDashboardMetrics(Integer storeId, String range);

    /**
     * 店员端桌位实时状态（L-1）
     * 包含 customer / arriveTime / estLeaveTime 等运营字段
     */
    List<Map<String, Object>> getStaffTables(Integer storeId);

    /**
     * 获取异常告警列表（L-2）
     * 如超时、未到店、设备故障等
     */
    List<Map<String, Object>> getAlerts(Integer storeId);

    /**
     * 获取门店订单列表（L-3）
     * 按门店查询所有预约/订单，供店员工作台"全部订单"Tab 使用
     */
    List<Map<String, Object>> getStaffOrders(Integer storeId);

    /**
     * 店员接单 — 确认到店（L-4）
     * 将预约状态从 BOOKED 改为 CONFIRMED，同时更新桌位状态为 OCCUPIED
     *
     * @param reservationId 预约 ID
     * @return 操作结果
     */
    Map<String, Object> acceptOrder(Long reservationId);

    /**
     * 桌位调度（L-5）
     * 更新桌位实时状态，若切为 available 则清除 currentReservationId
     *
     * @param tableId 桌位 ID
     * @param status  新状态（available / occupied / cleaning / reserved 等）
     * @return 操作结果
     */
    Map<String, Object> dispatchTable(Integer tableId, String status);

    /**
     * 订单进度推进（L-6）
     * 支持状态流转：CONFIRMED→MAKING, MAKING→SERVING, SERVING→COMPLETED
     * 若推进到 COMPLETED，同时将关联桌位状态设为 CLEANING
     *
     * @param reservationId 预约 ID
     * @param targetStatus   目标状态（MAKING / SERVING / COMPLETED）
     * @return 操作结果
     */
    Map<String, Object> progressOrder(Long reservationId, String targetStatus);

    /**
     * 获取退款申请列表（L-7）
     * 按门店查询退款记录，关查预约信息供店员审核
     *
     * @param storeId 门店 ID
     * @return 退款申请列表
     */
    List<Map<String, Object>> getRefundList(Integer storeId);

    /**
     * 审核退款（L-8）
     * 通过：更新退款记录状态为 APPROVED，更新预约状态为 CANCEL_ORDER
     * 拒绝：更新退款记录状态为 REJECTED，恢复预约状态为 CONFIRMED
     *
     * @param refundId    退款记录 ID
     * @param action      操作类型：approve / reject
     * @param operatorId  操作人用户 ID
     * @return 操作结果
     */
    Map<String, Object> reviewRefund(Long refundId, String action, Long operatorId);

    /**
     * 告警已知晓（L-9）
     * 店员确认收到告警，状态 PENDING → ACKNOWLEDGED
     *
     * @param exceptionId 异常记录 ID
     * @param operatorId  操作人用户 ID
     * @return 操作结果
     */
    Map<String, Object> acknowledgeAlert(Long exceptionId, Long operatorId);

    /**
     * 解决告警（L-10）
     * 店员完成告警处理，状态 ACKNOWLEDGED → RESOLVED
     *
     * @param exceptionId 异常记录 ID
     * @param resolution  解决说明
     * @param operatorId  操作人用户 ID
     * @return 操作结果
     */
    Map<String, Object> resolveAlert(Long exceptionId, String resolution, Long operatorId);
}
