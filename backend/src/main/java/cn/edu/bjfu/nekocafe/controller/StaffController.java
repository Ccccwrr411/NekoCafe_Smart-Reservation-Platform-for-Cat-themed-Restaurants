package cn.edu.bjfu.nekocafe.controller;

import cn.edu.bjfu.nekocafe.common.Result;
import cn.edu.bjfu.nekocafe.service.StaffService;
import cn.edu.bjfu.nekocafe.vo.DashboardMetricsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * 数据看板 & 店员后台 Controller
 * 负责人：___
 * 接口：K-1 GET /api/dashboard/metrics
 *       L-1 GET /api/staff/tables
 *       L-2 GET /api/staff/alerts
 *       L-3 GET /api/staff/orders
 *       L-4 POST /api/staff/order/accept
 *       L-5 POST /api/staff/table/dispatch
 */
@RestController
@RequestMapping("/api")
public class StaffController {

    @Autowired
    private StaffService staffService;

    /** K-1 运营指标看板 */
    @GetMapping("/dashboard/metrics")
    public Result<DashboardMetricsVO> getDashboardMetrics(@RequestParam Integer storeId,
                                                           @RequestParam String range) {
        return Result.success(staffService.getDashboardMetrics(storeId, range));
    }

    /** L-1 店员端桌位状态 */
    @GetMapping("/staff/tables")
    public Result<List<Map<String, Object>>> getStaffTables(@RequestParam Integer storeId) {
        return Result.success(staffService.getStaffTables(storeId));
    }

    /** L-2 异常告警 */
    @GetMapping("/staff/alerts")
    public Result<List<Map<String, Object>>> getAlerts(@RequestParam Integer storeId) {
        return Result.success(staffService.getAlerts(storeId));
    }

    /** L-3 门店订单列表 */
    @GetMapping("/staff/orders")
    public Result<List<Map<String, Object>>> getStaffOrders(@RequestParam Integer storeId) {
        return Result.success(staffService.getStaffOrders(storeId));
    }

    /** L-4 店员接单（确认到店） */
    @PostMapping("/staff/order/accept")
    public Result<Map<String, Object>> acceptOrder(@RequestBody Map<String, Object> body) {
        Long reservationId = Long.valueOf(body.get("reservationId").toString());
        return Result.success(staffService.acceptOrder(reservationId));
    }

    /** L-5 桌位调度 */
    @PostMapping("/staff/table/dispatch")
    public Result<Map<String, Object>> dispatchTable(@RequestBody Map<String, Object> body) {
        Integer tableId = Integer.valueOf(body.get("tableId").toString());
        String status = (String) body.get("status");
        return Result.success(staffService.dispatchTable(tableId, status));
    }

    /** L-6 订单进度推进 */
    @PostMapping("/staff/order/progress")
    public Result<Map<String, Object>> progressOrder(@RequestBody Map<String, Object> body) {
        Long reservationId = Long.valueOf(body.get("reservationId").toString());
        String targetStatus = (String) body.get("targetStatus");
        return Result.success(staffService.progressOrder(reservationId, targetStatus));
    }

    /** L-7 退款申请列表 */
    @GetMapping("/staff/refunds")
    public Result<List<Map<String, Object>>> getRefundList(@RequestParam Integer storeId) {
        return Result.success(staffService.getRefundList(storeId));
    }

    /** L-8 审核退款 */
    @PostMapping("/staff/refund/review")
    public Result<Map<String, Object>> reviewRefund(@RequestBody Map<String, Object> body) {
        Long refundId = Long.valueOf(body.get("refundId").toString());
        String action = (String) body.get("action");
        Long operatorId = body.get("operatorId") != null
                ? Long.valueOf(body.get("operatorId").toString()) : null;
        return Result.success(staffService.reviewRefund(refundId, action, operatorId));
    }

    /** L-9 告警已知晓 */
    @PostMapping("/staff/alert/acknowledge")
    public Result<Map<String, Object>> acknowledgeAlert(@RequestBody Map<String, Object> body) {
        Long exceptionId = Long.valueOf(body.get("exceptionId").toString());
        Long operatorId = body.get("operatorId") != null
                ? Long.valueOf(body.get("operatorId").toString()) : null;
        return Result.success(staffService.acknowledgeAlert(exceptionId, operatorId));
    }

    /** L-10 解决告警 */
    @PostMapping("/staff/alert/resolve")
    public Result<Map<String, Object>> resolveAlert(@RequestBody Map<String, Object> body) {
        Long exceptionId = Long.valueOf(body.get("exceptionId").toString());
        String resolution = (String) body.get("resolution");
        Long operatorId = body.get("operatorId") != null
                ? Long.valueOf(body.get("operatorId").toString()) : null;
        return Result.success(staffService.resolveAlert(exceptionId, resolution, operatorId));
    }
}
