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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 评价服务实现
 * 负责人：D 同学
 *
 * M-1: submitReview — 对已完成订单提交评价，验证归属+无重复，写 reviews 表，奖励积分
 * M-2: getReviewDetail — 查看某订单的已有评价
 *
 * 注意：
 *   1. 前端传 orderId（如 "ORD0000000123"），需解析为 reservationId(Long)
 *   2. Reservations 表无 has_review 字段，通过 Reviews 表反查是否已评价
 *   3. 积分奖励 +10，写入 points_log 并更新 member_ext.total_points
 *   4. tags 存为 JSONB，Java 层用 String 存 JSON 字符串
 *   5. status 统一用 "VISIBLE"（与 DB 默认值一致）
 */
@Service
public class ReviewServiceImpl implements ReviewService {

    private static final ObjectMapper objectMapper = new ObjectMapper();

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
        // 1. 解析 orderId -> reservationId
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
        review.setFoodRating(dto.getFoodRating());
        review.setServiceRating(dto.getServiceRating());
        review.setEnvironmentRating(dto.getEnvironmentRating());
        review.setCatInteractionRating(dto.getCatInteractionRating());
        review.setContent(dto.getContent());
        review.setStatus("VISIBLE");
        review.setCreatedAt(new Date());

        // tags: List<String> -> JSON 字符串
        if (dto.getTags() != null && !dto.getTags().isEmpty()) {
            try {
                review.setTags(objectMapper.writeValueAsString(dto.getTags()));
            } catch (Exception e) {
                review.setTags("[]");
            }
        }

        reviewsMapper.insertSelective(review);

        // 5. 积分奖励 +10
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
        result.put("status", "VISIBLE");
        result.put("pointsEarned", 10);
        return result;
    }

    // ==================== M-2 : 查看评价详情 ====================

    @Override
    public Map<String, Object> getReviewDetail(String orderId) {
        Long reservationId = parseOrderId(orderId);

        ReviewsExample re = new ReviewsExample();
        re.createCriteria().andReservationIdEqualTo(reservationId);
        List<Reviews> reviews = reviewsMapper.selectByExample(re);
        if (reviews == null || reviews.isEmpty()) {
            return null;
        }

        Reviews review = reviews.get(0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("reviewId", review.getReviewId());
        result.put("overallRating", review.getOverallRating());
        result.put("foodRating", review.getFoodRating());
        result.put("serviceRating", review.getServiceRating());
        result.put("environmentRating", review.getEnvironmentRating());
        result.put("catInteractionRating", review.getCatInteractionRating());
        result.put("content", review.getContent());
        result.put("tags", review.getTags());
        result.put("reply", review.getReply());
        result.put("replyAt", review.getReplyAt() != null ? sdf.format(review.getReplyAt()) : null);
        result.put("createdAt", review.getCreatedAt() != null ? sdf.format(review.getCreatedAt()) : null);
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
