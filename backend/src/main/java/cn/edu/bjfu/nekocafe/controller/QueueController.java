package cn.edu.bjfu.nekocafe.controller;

import cn.edu.bjfu.nekocafe.common.Result;
import cn.edu.bjfu.nekocafe.dto.QueueTakeDTO;
import cn.edu.bjfu.nekocafe.service.QueueService;
import cn.edu.bjfu.nekocafe.vo.QueueStatusVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * 排队 Controller
 * 负责人：E同学（lsf）
 * 接口：J-1 GET /api/queue/status?storeId=
 *       J-2 POST /api/queue/take
 */
@RestController
@RequestMapping("/api/queue")
public class QueueController {

    @Autowired
    private QueueService queueService;

    /** J-1 排队状态 */
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
        return Result.success(queueService.takeNumber(userId, dto));
    }

    /**
     * 解析当前用户ID（开发测试用）
     * 优先级：请求头 X-Test-UserId > JWT解析 > 默认值1
     * Apifox 测试时，在 Headers 中添加 X-Test-UserId 即可切换用户
     * 登录功能通后，删除此方法，直接用 request.getAttribute("userId")
     */
    private Long resolveUserId(HttpServletRequest request) {
        // 1. 优先读测试请求头（Apifox 中设置 X-Test-UserId 即可模拟不同用户）
        String testUserId = request.getHeader("X-Test-UserId");
        System.out.println("[DEBUG] X-Test-UserId header = " + testUserId);  // 调试用，上线前删除
        if (testUserId != null && !testUserId.isEmpty()) {
            try {
                return Long.parseLong(testUserId);
            } catch (NumberFormatException e) {
                // 忽略，继续走正常流程
            }
        }

        // 2. JWT 拦截器解析的 userId
        Long userId = (Long) request.getAttribute("userId");
        System.out.println("[DEBUG] JWT userId attribute = " + userId);  // 调试用，上线前删除

        // 3. 开发测试默认值
        if (userId == null) {
            userId = 1L;
        }
        System.out.println("[DEBUG] final userId = " + userId);  // 调试用，上线前删除
        return userId;
    }
}
