package cn.edu.bjfu.nekocafe.service.impl;

import cn.edu.bjfu.nekocafe.dto.RealnameDTO;
import cn.edu.bjfu.nekocafe.entity.MemberExt;
import cn.edu.bjfu.nekocafe.entity.ReservationsExample;
import cn.edu.bjfu.nekocafe.entity.UserCouponsExample;
import cn.edu.bjfu.nekocafe.entity.Users;
import cn.edu.bjfu.nekocafe.mapper.MemberExtMapper;
import cn.edu.bjfu.nekocafe.mapper.ReservationsMapper;
import cn.edu.bjfu.nekocafe.mapper.UserCouponsMapper;
import cn.edu.bjfu.nekocafe.mapper.UsersMapper;
import cn.edu.bjfu.nekocafe.service.UserService;
import cn.edu.bjfu.nekocafe.vo.UserProfileVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 用户服务实现
 *
 * 实现要点：
 *   getProfile: Users JOIN MemberExt 联查
 *     - phone 脱敏：138****8888
 *     - level 映射：1=普通会员, 2=银卡会员, 3=金卡会员, 4=黑卡会员
 *     - 图标：🌱/🥈/🥇/⬛
 *     - 升级所需积分：普通→银卡 1000, 银卡→金卡 3000, 金卡→黑卡 10000
 *   verifyRealname: 写入 realName + idCard + isVerified=true
 *     - 身份证脱敏返回：前6后4可见，中间用 * 替换
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UsersMapper usersMapper;

    @Autowired
    private MemberExtMapper memberExtMapper;

    @Autowired
    private UserCouponsMapper userCouponsMapper;

    @Autowired
    private ReservationsMapper reservationsMapper;

    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public UserProfileVO getProfile(Long userId) {
        // 1. 查用户基础信息
        Users user = usersMapper.selectByPrimaryKey(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在: " + userId);
        }

        // 2. 查会员扩展信息（积分、等级）
        MemberExt memberExt = memberExtMapper.selectByPrimaryKey(userId);
        int level = (memberExt != null && memberExt.getLevel() != null) ? memberExt.getLevel() : 1;
        int points = (memberExt != null && memberExt.getTotalPoints() != null) ? memberExt.getTotalPoints() : 0;
        BigDecimal cumAmount = (memberExt != null && memberExt.getCumulativeAmount() != null) ? memberExt.getCumulativeAmount() : BigDecimal.ZERO;

        // 3. 统计订单数
        ReservationsExample resEx = new ReservationsExample();
        resEx.createCriteria().andUserIdEqualTo(userId);
        int totalOrders = (int) reservationsMapper.countByExample(resEx);

        // 4. 统计该用户的优惠券数
        UserCouponsExample couponEx = new UserCouponsExample();
        couponEx.createCriteria().andUserIdEqualTo(userId);
        int couponCount = (int) userCouponsMapper.countByExample(couponEx);

        // 5. 组装 VO
        UserProfileVO vo = new UserProfileVO();
        vo.setId(user.getUserId());
        vo.setNickName(user.getNickname());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setPhone(maskPhone(user.getPhone()));
        vo.setMemberLevel(levelToString(level));
        vo.setMemberLevelIcon(levelToIcon(level));
        vo.setPoints(points);
        vo.setPointsToNext(pointsToNext(level, points));
        vo.setNextLevel(nextLevelName(level));
        vo.setTotalOrders(totalOrders);
        vo.setTotalSpent(cumAmount.intValue());
        vo.setCouponCount(couponCount);
        vo.setFavoriteStores(Collections.emptyList());  // TODO: 需收藏表支持
        if (user.getCreatedAt() != null) {
            vo.setJoinDate(DATE_FMT.format(user.getCreatedAt()));
        }

        return vo;
    }

    @Override
    public Map<String, Object> verifyRealname(Long userId, RealnameDTO dto) {
        if (dto.getRealName() == null || dto.getRealName().isEmpty()) {
            throw new IllegalArgumentException("真实姓名不能为空");
        }
        if (dto.getIdCard() == null || dto.getIdCard().isEmpty()) {
            throw new IllegalArgumentException("身份证号不能为空");
        }

        // 更新 users 表
        Users user = new Users();
        user.setUserId(userId);
        user.setRealName(dto.getRealName());
        user.setIdCard(dto.getIdCard());
        user.setIsVerified(true);
        user.setUpdatedAt(new Date());
        usersMapper.updateByPrimaryKeySelective(user);

        // 返回脱敏身份证号
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("verified", true);
        result.put("realName", dto.getRealName());
        result.put("idCardMask", maskIdCard(dto.getIdCard()));
        return result;
    }

    // ========== 工具方法 ==========

    /** 手机号脱敏 */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return phone;
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    /** 身份证脱敏：前6后4可见 */
    private String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 10) return idCard;
        String prefix = idCard.substring(0, 6);
        String suffix = idCard.substring(idCard.length() - 4);
        return prefix + "********" + suffix;
    }

    /** 等级数字 → 中文 */
    private String levelToString(int level) {
        switch (level) {
            case 2: return "银卡会员";
            case 3: return "金卡会员";
            case 4: return "黑卡会员";
            default: return "普通会员";
        }
    }

    /** 等级 → 图标 */
    private String levelToIcon(int level) {
        switch (level) {
            case 2: return "🥈";
            case 3: return "🥇";
            case 4: return "⬛";
            default: return "🌱";
        }
    }

    /** 下一级所需积分 */
    private int pointsToNext(int currentLevel, int currentPoints) {
        int nextThreshold = switch (currentLevel) {
            case 1 -> 1000;   // 普通→银卡
            case 2 -> 3000;   // 银卡→金卡
            case 3 -> 10000;  // 金卡→黑卡
            default -> Integer.MAX_VALUE; // 黑卡无下一级
        };
        return Math.max(0, nextThreshold - currentPoints);
    }

    /** 下一级名称 */
    private String nextLevelName(int currentLevel) {
        return switch (currentLevel) {
            case 4 -> "最高等级";
            default -> levelToString(currentLevel + 1);
        };
    }
}
