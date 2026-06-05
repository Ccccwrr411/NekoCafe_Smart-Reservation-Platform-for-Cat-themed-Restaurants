package cn.edu.bjfu.nekocafe.controller;

import cn.edu.bjfu.nekocafe.common.Result;
import cn.edu.bjfu.nekocafe.service.MenuService;
import cn.edu.bjfu.nekocafe.vo.MenuVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 菜单 Controller
 * 负责人：___
 * 接口：D-1 GET /api/menu?storeId=
 */
@RestController
@RequestMapping("/api")
public class MenuController {

    @Autowired
    private MenuService menuService;

    /** D-1 菜品列表 */
    @GetMapping("/menu")
    public Result<MenuVO> getMenu(@RequestParam Integer storeId) {
        return Result.success(menuService.getMenu(storeId));
    }
}
