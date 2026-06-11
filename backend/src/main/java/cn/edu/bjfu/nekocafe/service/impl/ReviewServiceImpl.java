package cn.edu.bjfu.nekocafe.service.impl;

import cn.edu.bjfu.nekocafe.dto.ReviewSubmitDTO;
import cn.edu.bjfu.nekocafe.mapper.PointsLogMapper;
import cn.edu.bjfu.nekocafe.mapper.ReviewsMapper;
import cn.edu.bjfu.nekocafe.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;

/**
 * 评价服务实现
 * 负责人：___
 *
 * 实现要点：
 *   1. 校验该订单是否属于当前用户，且 status=completed，且尚未评价
 *   2. 插入 reviews 表
 *   3. 更新 reservations.has_review = true（或用 reviews 表查询代替）
 *   4. 向 points_log 表写入积分记录（+10），更新 member_ext.total_points
 *   5. 生成 reviewId = "REV" + reviews.review_id（补零到10位）
 */
@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewsMapper reviewsMapper;

    @Autowired
    private PointsLogMapper pointsLogMapper;

    @Override
    public Map<String, Object> submitReview(Long userId, ReviewSubmitDTO dto) {
        throw new UnsupportedOperationException("ReviewServiceImpl.submitReview 尚未实现");
    }
}
