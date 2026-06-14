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
    Map<String, Object> reviewRefund(Long refundId, String action, Long operatorId);
    Map<String, Object> acknowledgeAlert(Long exceptionId, Long operatorId);
    Map<String, Object> resolveAlert(Long exceptionId, String resolution, Long operatorId);
}
