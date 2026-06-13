package cn.edu.bjfu.nekocafe.controller;

import cn.edu.bjfu.nekocafe.common.Result;
import cn.edu.bjfu.nekocafe.service.TableService;
import cn.edu.bjfu.nekocafe.vo.TableVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 桌位 Controller
 * 负责人：___
 * 接口：C-1 GET /api/tables?storeId=
 */
@RestController
@RequestMapping("/api")
public class TableController {

    @Autowired
    private TableService tableService;

    /** C-1 桌位列表（支持时段冲突判断） */
    @GetMapping("/tables")
    public Result<List<TableVO>> listTables(
            @RequestParam Integer storeId,
            @RequestParam(required = false) String reserveDate,
            @RequestParam(required = false) String reserveTime,
            @RequestParam(required = false) Integer duration) {
        return Result.success(tableService.listTables(storeId, reserveDate, reserveTime, duration));
    }
}
