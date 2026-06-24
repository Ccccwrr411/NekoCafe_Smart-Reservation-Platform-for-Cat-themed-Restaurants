package cn.edu.bjfu.nekocafe.controller;

import cn.edu.bjfu.nekocafe.common.Result;
import cn.edu.bjfu.nekocafe.dto.ReviewSubmitDTO;
import cn.edu.bjfu.nekocafe.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * 评价 Controller
 * 负责人：___
 * 接口：
 *   M-1 POST /api/review/submit   — 提交评价
 *   M-2 GET  /api/review/detail   — 查看某订单的已有评价
 */
@RestController
@RequestMapping("/api")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    /** M-1 提交评价 */
    @PostMapping("/review/submit")
    public Result<Map<String, Object>> submitReview(@RequestBody ReviewSubmitDTO dto,
                                                     HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(reviewService.submitReview(userId, dto));
    }

    /** M-2 查看某订单的已有评价 */
    @GetMapping("/review/detail")
    public Result<Map<String, Object>> getReviewDetail(@RequestParam String orderId) {
        Map<String, Object> detail = reviewService.getReviewDetail(orderId);
        return Result.success(detail);
    }
}
