package cn.edu.bjfu.nekocafe.service;

import cn.edu.bjfu.nekocafe.dto.ReviewSubmitDTO;
import java.util.Map;

/**
 * 评价服务接口
 * 实现类：ReviewServiceImpl
 */
public interface ReviewService {

    /**
     * 提交评价（M-1）
     * 返回 reviewId + status + pointsEarned
     */
    Map<String, Object> submitReview(Long userId, ReviewSubmitDTO dto);
}
