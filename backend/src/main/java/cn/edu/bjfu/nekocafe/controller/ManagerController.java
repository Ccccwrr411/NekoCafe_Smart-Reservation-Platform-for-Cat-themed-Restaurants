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
}
