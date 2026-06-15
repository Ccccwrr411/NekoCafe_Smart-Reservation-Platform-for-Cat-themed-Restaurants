package cn.edu.bjfu.nekocafe.controller;

import cn.edu.bjfu.nekocafe.common.Result;
import cn.edu.bjfu.nekocafe.service.StoreService;
import cn.edu.bjfu.nekocafe.vo.StoreVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 门店 Controller
 * 负责人：___
 * 接口：B-1 GET /api/stores
 */
@RestController
@RequestMapping("/api")
public class StoreController {

    @Autowired
    private StoreService storeService;

    /** B-1 门店列表 */
    @GetMapping("/stores")
    public Result<List<StoreVO>> listStores() {
        return Result.success(storeService.listStores());
    }
}
