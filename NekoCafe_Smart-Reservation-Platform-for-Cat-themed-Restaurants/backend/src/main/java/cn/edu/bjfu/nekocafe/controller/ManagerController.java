package cn.edu.bjfu.nekocafe.controller;

import cn.edu.bjfu.nekocafe.common.Result;
import cn.edu.bjfu.nekocafe.service.ManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/manager")
public class ManagerController {

    @Autowired
    private ManagerService managerService;

    @GetMapping("/schedules")
    public Result<List<Map<String, Object>>> getSchedules(@RequestParam Integer storeId) {
        return Result.success(managerService.getSchedules(storeId));
    }

    @GetMapping("/shifts")
    public Result<List<Map<String, Object>>> getShifts() {
        return Result.success(managerService.getShifts());
    }

    @GetMapping("/exceptions")
    public Result<List<Map<String, Object>>> getExceptions(@RequestParam Integer storeId) {
        return Result.success(managerService.getExceptions(storeId));
    }

    @PostMapping("/exception/review")
    public Result<Map<String, Object>> reviewException(@RequestBody Map<String, Object> body) {
        Long exceptionId = Long.valueOf(body.get("exceptionId").toString());
        String action = (String) body.get("action");
        return Result.success(managerService.reviewException(exceptionId, action));
    }

    /**
     * 创建排班
     * POST /api/manager/schedule
     * Body: { storeId, staffId, workDate, shiftId, startTime?, endTime?, position?, notes? }
     */
    @PostMapping("/schedule")
    public Result<Map<String, Object>> createSchedule(@RequestBody Map<String, Object> body) {
        return Result.success(managerService.createSchedule(body));
    }

    /**
     * 修改排班
     * PUT /api/manager/schedule/{scheduleId}
     * Body: { workDate?, shiftId?, startTime?, endTime?, staffId?, position?, notes? }
     */
    @PutMapping("/schedule/{scheduleId}")
    public Result<Map<String, Object>> updateSchedule(
            @PathVariable Long scheduleId,
            @RequestBody Map<String, Object> body) {
        return Result.success(managerService.updateSchedule(scheduleId, body));
    }

    /**
     * 按门店搜索员工（支持姓名/昵称模糊搜索）
     * GET /api/manager/staff?storeId=&keyword=
     */
    @GetMapping("/staff")
    public Result<List<Map<String, Object>>> searchStaff(
            @RequestParam Integer storeId,
            @RequestParam(required = false, defaultValue = "") String keyword) {
        return Result.success(managerService.searchStaff(storeId, keyword));
    }
}
