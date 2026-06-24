package cn.edu.bjfu.nekocafe.service;

import cn.edu.bjfu.nekocafe.dto.ReviewSubmitDTO;
import java.util.Map;

/**
 * 评价服务接口
 * 实现类：ReviewServiceImpl
 */
public interface ReviewService {

    /**
     * M-1 提交评价
     * 返回 reviewId + status + pointsEarned
     */
    Map<String, Object> submitReview(Long userId, ReviewSubmitDTO dto);

    /**
     * M-2 查看某订单的已有评价
     * 返回 null 表示未评价
     */
    Map<String, Object> getReviewDetail(String orderId);
}
