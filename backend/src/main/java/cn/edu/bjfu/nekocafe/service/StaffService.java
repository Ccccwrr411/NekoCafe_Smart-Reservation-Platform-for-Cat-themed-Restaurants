package cn.edu.bjfu.nekocafe.service;

import cn.edu.bjfu.nekocafe.vo.DashboardMetricsVO;
import java.util.List;
import java.util.Map;

public interface StaffService {
    DashboardMetricsVO getDashboardMetrics(Integer storeId, String range);
    List<Map<String, Object>> getStaffTables(Integer storeId);
    List<Map<String, Object>> getAlerts(Integer storeId);
    List<Map<String, Object>> getStaffOrders(Integer storeId);
    Map<String, Object> acceptOrder(Long reservationId);
    Map<String, Object> dispatchTable(Integer tableId, String status);
    Map<String, Object> progressOrder(Long reservationId, String targetStatus);
    List<Map<String, Object>> getRefundList(Integer storeId);

    /**
     * 审核退款（L-8）
     * 通过：更新退款记录状态为 APPROVED，预约状态 → CANCEL_ORDER，释放桌位，扣减积分/累计消费
     * 拒绝：更新退款记录状态为 REJECTED，预约保持 REFUNDING（前端展示"售后被拒绝"），
     *       释放桌位，恢复支付状态为 PAID（积分/金额在申请时未扣减，无需恢复）
     *
     * @param refundId     退款记录 ID
     * @param action       操作类型：approve / reject
     * @param operatorId   操作人用户 ID
     * @param rejectReason 拒绝原因（仅 reject 时使用，写入 refund_reason 字段）
     * @return 操作结果
     */
    Map<String, Object> reviewRefund(Long refundId, String action, Long operatorId, String rejectReason);

    /**
     * 告警已知晓（L-9）
     * 店员确认收到告警，状态 PENDING → ACKNOWLEDGED
     *
     * @param exceptionId 异常记录 ID
     * @param operatorId  操作人用户 ID
     * @return 操作结果
     */
    Map<String, Object> acknowledgeAlert(Long exceptionId, Long operatorId);
    
    Map<String, Object> resolveAlert(Long exceptionId, String resolution, Long operatorId);
}