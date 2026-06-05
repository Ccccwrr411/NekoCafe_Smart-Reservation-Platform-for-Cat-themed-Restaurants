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
 * 负责人：___
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
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(queueService.getQueueStatus(storeId, userId));
    }

    /** J-2 取号 */
    @PostMapping("/take")
    public Result<Map<String, Object>> takeNumber(@RequestBody QueueTakeDTO dto,
                                                   HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(queueService.takeNumber(userId, dto));
    }
}
