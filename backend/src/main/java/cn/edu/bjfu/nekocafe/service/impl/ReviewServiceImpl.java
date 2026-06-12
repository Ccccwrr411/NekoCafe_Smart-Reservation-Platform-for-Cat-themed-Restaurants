package cn.edu.bjfu.nekocafe.service.impl;

import cn.edu.bjfu.nekocafe.common.ErrorCode;
import cn.edu.bjfu.nekocafe.dto.ReviewSubmitDTO;
import cn.edu.bjfu.nekocafe.entity.*;
import cn.edu.bjfu.nekocafe.exception.BusinessException;
import cn.edu.bjfu.nekocafe.mapper.MemberExtMapper;
import cn.edu.bjfu.nekocafe.mapper.PointsLogMapper;
import cn.edu.bjfu.nekocafe.mapper.ReservationsMapper;
import cn.edu.bjfu.nekocafe.mapper.ReviewsMapper;
import cn.edu.bjfu.nekocafe.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 评价服务实现
 * 负责人：D 同学
 *
 * M-1: submitReview — 对已完成订单提交评价，验证归属+无重复，写 reviews 表，奖励积分
 *
 * 注意：
 *   1. 前端传 orderId（如 "ORD0000000123"），需解析为 reservationId(Long)
 *   2. Reservations 表无 has_review 字段，通过 Reviews 表反查是否已评价
 *   3. 积分奖励 +10，写入 points_log 并更新 member_ext.total_points
 */
@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewsMapper reviewsMapper;

    @Autowired
    private ReservationsMapper reservationsMapper;

    @Autowired
    private PointsLogMapper pointsLogMapper;

    @Autowired
    private MemberExtMapper memberExtMapper;

    // ==================== M-1 : 提交评价 ====================

    @Override
    public Map<String, Object> submitReview(Long userId, ReviewSubmitDTO dto) {
        // 1. 解析 orderId → reservationId
        Long reservationId = parseOrderId(dto.getOrderId());

        // 2. 校验订单存在且属于当前用户
        Reservations reservation = reservationsMapper.selectByPrimaryKey(reservationId);
        if (reservation == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在");
        }
        if (!reservation.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权评价此订单");
        }
        if (!"COMPLETED".equals(reservation.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "只能评价已完成的订单");
        }

        // 3. 检查是否已评价（通过 reviews 表反查）
        ReviewsExample re = new ReviewsExample();
        re.createCriteria().andReservationIdEqualTo(reservationId);
        long existing = reviewsMapper.countByExample(re);
        if (existing > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "该订单已评价，不可重复提交");
        }

        // 4. 写入 reviews 表
        Reviews review = new Reviews();
        review.setReservationId(reservationId);
        review.setUserId(userId);
        review.setStoreId(reservation.getStoreId());
        review.setOverallRating(dto.getRating());
        review.setContent(dto.getContent());
        review.setStatus("published");
        review.setCreatedAt(new Date());
        reviewsMapper.insertSelective(review);

        // 5. 积分奖励 +10
        // 先查当前积分
        MemberExt member = memberExtMapper.selectByPrimaryKey(userId);
        int currentPoints = (member != null && member.getTotalPoints() != null)
                ? member.getTotalPoints() : 0;
        int newPoints = currentPoints + 10;

        // 写积分日志
        PointsLog log = new PointsLog();
        log.setUserId(userId);
        log.setChangeAmount(10);
        log.setBalanceAfter(newPoints);
        log.setSource("review");
        log.setReservationId(reservationId);
        log.setCreatedAt(new Date());
        pointsLogMapper.insertSelective(log);

        // 更新会员积分
        if (member != null) {
            member.setTotalPoints(newPoints);
            memberExtMapper.updateByPrimaryKeySelective(member);
        }

        // 6. 返回结果
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("reviewId", review.getReviewId());
        result.put("status", "published");
        result.put("pointsEarned", 10);
        return result;
    }

    // ==================== 私有辅助方法 ====================

    /** 将 "ORD0000000123" 解析为 Long(123) */
    private Long parseOrderId(String orderId) {
        if (orderId == null || orderId.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "orderId 不能为空");
        }
        String numStr = orderId.startsWith("ORD") ? orderId.substring(3) : orderId;
        try {
            return Long.parseLong(numStr);
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "orderId 格式错误: " + orderId);
        }
    }
}
