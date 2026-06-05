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
}
