package cn.edu.bjfu.nekocafe.controller;

import cn.edu.bjfu.nekocafe.common.Result;
import cn.edu.bjfu.nekocafe.service.RecommendService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * AI 推荐 Controller
 * 负责人：___（最后实现）
 * 接口：H-1 GET /api/recommend?userId=
 */
@RestController
@RequestMapping("/api")
public class RecommendController {

    @Autowired
    private RecommendService recommendService;

    /** H-1 个性化推荐 */
    @GetMapping("/recommend")
    public Result<Map<String, Object>> recommend(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(recommendService.recommend(userId));
    }
}
