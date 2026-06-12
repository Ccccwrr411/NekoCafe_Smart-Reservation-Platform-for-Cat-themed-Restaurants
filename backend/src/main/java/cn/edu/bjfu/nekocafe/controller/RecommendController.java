package cn.edu.bjfu.nekocafe.controller;

import cn.edu.bjfu.nekocafe.common.Result;
import cn.edu.bjfu.nekocafe.service.RecommendService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * AI 推荐 Controller
 * 接口：H-1 GET /api/recommend?userId=&companionCount=&hasChild=
 *
 * 上下文参数（可选）：
 *   companionCount — 同行人数（默认=1），用于 R10 桌位匹配
 *   hasChild       — 是否带小孩（默认=false），用于 R11 温顺猫筛选
 */
@RestController
@RequestMapping("/api")
public class RecommendController {

    @Autowired
    private RecommendService recommendService;

    /** H-1 个性化推荐 */
    @GetMapping("/recommend")
    public Result<Map<String, Object>> recommend(
            HttpServletRequest request,
            @RequestParam(required = false, defaultValue = "1") Integer companionCount,
            @RequestParam(required = false, defaultValue = "false") Boolean hasChild) {

        // 开发阶段：优先从参数取 userId，上线后恢复为从 JWT 取
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            // 测试用：允许通过 ?userId=xxx 传入
            String param = request.getParameter("userId");
            if (param != null && !param.isEmpty()) {
                userId = Long.valueOf(param);
            }
        }

        return Result.success(recommendService.recommend(userId, companionCount, hasChild));
    }
}
