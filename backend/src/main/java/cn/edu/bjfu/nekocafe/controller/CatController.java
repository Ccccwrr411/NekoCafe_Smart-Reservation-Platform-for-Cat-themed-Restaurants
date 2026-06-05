package cn.edu.bjfu.nekocafe.controller;

import cn.edu.bjfu.nekocafe.common.Result;
import cn.edu.bjfu.nekocafe.service.CatService;
import cn.edu.bjfu.nekocafe.vo.CatVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 猫咪档案 Controller
 * 负责人：___
 * 接口：I-1 GET /api/cats?storeId=
 *       I-2 GET /api/cats/detail?catId=
 */
@RestController
@RequestMapping("/api")
public class CatController {

    @Autowired
    private CatService catService;

    /** I-1 猫咪列表 */
    @GetMapping("/cats")
    public Result<List<CatVO>> listCats(@RequestParam Integer storeId) {
        return Result.success(catService.listCats(storeId));
    }

    /** I-2 猫咪详情 */
    @GetMapping("/cats/detail")
    public Result<CatVO> getCatDetail(@RequestParam Integer catId) {
        return Result.success(catService.getCatDetail(catId));
    }
}
