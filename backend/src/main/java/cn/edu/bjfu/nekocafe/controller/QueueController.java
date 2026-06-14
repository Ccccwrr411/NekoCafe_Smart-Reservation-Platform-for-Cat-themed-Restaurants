package cn.edu.bjfu.nekocafe.controller;

import cn.edu.bjfu.nekocafe.common.Result;
import cn.edu.bjfu.nekocafe.dto.QueueTakeDTO;
import cn.edu.bjfu.nekocafe.service.QueueService;
import cn.edu.bjfu.nekocafe.util.JwtUtil;
import cn.edu.bjfu.nekocafe.vo.QueueStatusVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * 排队 Controller
 * 负责人：E同学（lsf）
 * 接口：J-1 GET  /api/queue/status?storeId=
 *       J-2 POST /api/queue/take
 *       J-3 POST /api/queue/call          店员叫号
 *       J-4 POST /api/queue/confirm        用户确认叫号
 */
@RestController
@RequestMapping("/api/queue")
public class QueueController {

    @Autowired
    private QueueService queueService;

    /** J-1 排队状态（允许未登录查看，只是 myNumber 为空） */
    @GetMapping("/status")
    public Result<QueueStatusVO> getQueueStatus(@RequestParam Integer storeId,
                                                  HttpServletRequest request) {
        Long userId = resolveUserId(request);
        return Result.success(queueService.getQueueStatus(storeId, userId));
    }

    /** J-2 取号 */
    @PostMapping("/take")
    public Result<Map<String, Object>> takeNumber(@RequestBody QueueTakeDTO dto,
                                                   HttpServletRequest request) {
        Long userId = resolveUserId(request);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success(queueService.takeNumber(userId, dto));
    }

    /** J-3 叫号 — 店员操作，将指定排队记录标记为 CALLED */
    @PostMapping("/call")
    public Result<Map<String, Object>> callNumber(@RequestBody Map<String, Object> body) {
        Integer storeId = (Integer) body.get("storeId");
        Long queueId = ((Number) body.get("queueId")).longValue();
        return Result.success(queueService.callNumber(storeId, queueId));
    }

    /** J-4 确认叫号 — 用户操作，将 CALLED 改为 KNOWN */
    @PostMapping("/confirm")
    public Result<Void> confirmNumber(@RequestBody Map<String, Object> body,
                                        HttpServletRequest request) {
        Long userId = resolveUserId(request);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        Long queueId = ((Number) body.get("queueId")).longValue();
        queueService.confirmNumber(userId, queueId);
        return Result.success(null);
    }

    /**
     * 解析当前用户ID
     * 优先级：请求头 X-Test-UserId > JWT拦截器解析 > Authorization头手动解析 > null
     *
     * 注意：/api/queue/status 被排除在 JWT 拦截器之外，
     * 但已登录用户仍然需要识别身份（isMine 标记），
     * 所以这里手动从 Authorization 头解析 token 作为 fallback。
     */
    private Long resolveUserId(HttpServletRequest request) {
        // 1. 优先读测试请求头
        String testUserId = request.getHeader("X-Test-UserId");
        if (testUserId != null && !testUserId.isEmpty()) {
            try {
                return Long.parseLong(testUserId);
            } catch (NumberFormatException e) { }
        }

        // 2. JWT 拦截器解析的 userId
        Long userId = (Long) request.getAttribute("userId");
        if (userId != null) return userId;

        // 3. 手动解析 Authorization 头（兼容 /api/queue/status 不走拦截器的场景）
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (JwtUtil.validateToken(token)) {
                return JwtUtil.getUserIdFromToken(token);
            }
        }

        return null;
    }
}
