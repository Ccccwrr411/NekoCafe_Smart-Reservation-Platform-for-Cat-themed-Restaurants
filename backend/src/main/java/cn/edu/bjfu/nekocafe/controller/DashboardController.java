package cn.edu.bjfu.nekocafe.controller;

import cn.edu.bjfu.nekocafe.common.Result;
import cn.edu.bjfu.nekocafe.service.DashboardService;
import cn.edu.bjfu.nekocafe.vo.DashboardMetricsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

//    @GetMapping("/metrics")
//    public Result<DashboardMetricsVO> getMetrics(
//            @RequestParam Integer storeId,
//            @RequestParam(defaultValue = "7d") String range) {
//        return Result.success(dashboardService.getMetrics(storeId, range));
//    }
}
