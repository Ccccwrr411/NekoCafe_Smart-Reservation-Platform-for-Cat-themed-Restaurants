package cn.edu.bjfu.nekocafe.service.impl;

import cn.edu.bjfu.nekocafe.dto.RealnameDTO;
import cn.edu.bjfu.nekocafe.dto.UserUpdateDTO;
import cn.edu.bjfu.nekocafe.entity.MemberExt;
import cn.edu.bjfu.nekocafe.entity.ReservationsExample;
import cn.edu.bjfu.nekocafe.entity.Stores;
import cn.edu.bjfu.nekocafe.entity.UserCouponsExample;
import cn.edu.bjfu.nekocafe.entity.UserRoles;
import cn.edu.bjfu.nekocafe.entity.UserRolesExample;
import cn.edu.bjfu.nekocafe.entity.Users;
import cn.edu.bjfu.nekocafe.mapper.*;
import cn.edu.bjfu.nekocafe.service.UserService;
import cn.edu.bjfu.nekocafe.vo.UserProfileVO;
import org.mindrot.jbcrypt.BCrypt;
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
     *     - phone 脱敏：138****8888（用户绑定真实手机号后才有值）
     *     - email：直接返回，未绑定时为 null
     *     - openid：不对外暴露
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

    @Autowired
    private StoresMapper storesMapper;

    @Autowired
    private UserRolesMapper userRolesMapper;

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
        int dbLevel = (memberExt != null && memberExt.getLevel() != null) ? memberExt.getLevel() : 1;
        int points = (memberExt != null && memberExt.getTotalPoints() != null) ? memberExt.getTotalPoints() : 0;
        // 根据当前总积分重新计算真实等级（积分可能已超过数据库存储的 level）
        int computedLevel = computeLevelByPoints(points);
        int level = Math.max(dbLevel, computedLevel); // 取较大值，防止等级回退
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
        vo.setPhone(maskPhone(user.getPhone()));    // phone 现在才是真实手机号（可能为null）
        vo.setEmail(user.getEmail());               // 邮箱字段（可能为null）
        vo.setMemberLevel(levelToString(level));
        vo.setMemberLevelIcon(levelToIcon(level));
        vo.setPoints(points);
        vo.setPointsToNext(pointsToNext(level, points));
        vo.setLevelStartPoints(currentLevelStartPoints(level));
        int nextThresh = (level >= 5) ? LEVEL_THRESHOLDS[4] : LEVEL_THRESHOLDS[level];
        vo.setNextLevelThreshold(nextThresh);
        vo.setNextLevel(nextLevelName(level));
        vo.setTotalOrders(totalOrders);
        vo.setTotalSpent(cumAmount.intValue());
        vo.setCouponCount(couponCount);
        vo.setFavoriteStores(Collections.emptyList());  // TODO: 需收藏表支持

        // 查询用户绑定的门店信息（从 user_roles 表获取 store_id，保证数据准确）
        UserRolesExample ure = new UserRolesExample();
        ure.createCriteria().andUserIdEqualTo(userId);
        List<UserRoles> userRolesList = userRolesMapper.selectByExample(ure);
        if (!userRolesList.isEmpty()) {
            UserRoles ur = userRolesList.get(0);
            Integer storeId = ur.getStoreId();
            if (storeId != null) {
                vo.setStoreId(storeId);
                Stores store = storesMapper.selectByPrimaryKey(storeId);
                if (store != null) {
                    vo.setStoreName(store.getName());
                }
            }
        }

        if (user.getCreatedAt() != null) {
            vo.setJoinDate(DATE_FMT.format(user.getCreatedAt()));
        }

        // 实名认证信息
        boolean isVerified = user.getIsVerified() != null && user.getIsVerified();
        vo.setIsVerified(isVerified);
        if (isVerified) {
            vo.setRealName(user.getRealName());
            vo.setIdCardMask(maskIdCard(user.getIdCard()));
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

    @Override
    public UserProfileVO updateProfile(Long userId, UserUpdateDTO dto) {
        Users user = usersMapper.selectByPrimaryKey(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在: " + userId);
        }

        Users update = new Users();
        update.setUserId(userId);
        boolean hasChange = false;

        if (dto.getNickName() != null && !dto.getNickName().isEmpty()) {
            update.setNickname(dto.getNickName());
            hasChange = true;
        }
        if (dto.getAvatarUrl() != null && !dto.getAvatarUrl().isEmpty()) {
            update.setAvatarUrl(dto.getAvatarUrl());
            hasChange = true;
        }
        if (dto.getPhone() != null && !dto.getPhone().isEmpty()) {
            if (!dto.getPhone().matches("^1\\d{10}$")) {
                throw new IllegalArgumentException("手机号格式不正确");
            }
            update.setPhone(dto.getPhone());
            hasChange = true;
        }
        if (dto.getEmail() != null && !dto.getEmail().isEmpty()) {
            if (!dto.getEmail().matches("^[\\w.-]+@[\\w.-]+\\.\\w+$")) {
                throw new IllegalArgumentException("邮箱格式不正确");
            }
            update.setEmail(dto.getEmail());
            hasChange = true;
        }

        if (hasChange) {
            update.setUpdatedAt(new Date());
            usersMapper.updateByPrimaryKeySelective(update);
        }

        return getProfile(userId);
    }

    @Override
    public Map<String, Object> changePassword(Long userId, String oldPassword, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("新密码至少6位");
        }

        Users user = usersMapper.selectByPrimaryKey(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在: " + userId);
        }

        // 如果已有密码，需要验证旧密码
        if (user.getPasswordHash() != null && !user.getPasswordHash().isEmpty()) {
            if (oldPassword == null || oldPassword.isEmpty()) {
                throw new IllegalArgumentException("请输入旧密码");
            }
            if (!BCrypt.checkpw(oldPassword, user.getPasswordHash())) {
                throw new IllegalArgumentException("旧密码不正确");
            }
        }

        Users update = new Users();
        update.setUserId(userId);
        update.setPasswordHash(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
        update.setUpdatedAt(new Date());
        usersMapper.updateByPrimaryKeySelective(update);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("message", "密码修改成功");
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

    /** 等级数字 → 中文（5级体系） */
    private String levelToString(int level) {
        return switch (level) {
            case 1 -> "新注册用户";
            case 2 -> "初级会员";
            case 3 -> "中级会员";
            case 4 -> "高级会员";
            case 5 -> "高消费用户";
            default -> "新注册用户";
        };
    }

    /** 等级 → 图标 */
    private String levelToIcon(int level) {
        return switch (level) {
            case 1 -> "🌱";
            case 2 -> "⭐";
            case 3 -> "🌟";
            case 4 -> "💎";
            case 5 -> "👑";
            default -> "🌱";
        };
    }

    /**
     * 升级阈值（按总积分划分）
     * 1级:    0 ~ 1999
     * 2级: 2000 ~ 5999
     * 3级: 6000 ~ 14999
     * 4级: 15000 ~ 39999
     * 5级: 40000+
     */
    private static final int[] LEVEL_THRESHOLDS = {0, 2000, 6000, 15000, 40000, Integer.MAX_VALUE};

    /** 下一级所需积分 */
    private int pointsToNext(int currentLevel, int currentPoints) {
        if (currentLevel >= 5) return 0; // 已满级
        int nextThreshold = LEVEL_THRESHOLDS[currentLevel]; // thresholds[1]=500, [2]=1500, ...
        return Math.max(0, nextThreshold - currentPoints);
    }

    /** 当前等级起始积分（用于进度条计算） */
    private int currentLevelStartPoints(int currentLevel) {
        if (currentLevel <= 1) return 0;
        return LEVEL_THRESHOLDS[currentLevel - 1]; // e.g. level=3 → 500
    }

    /** 下一级名称 */
    private String nextLevelName(int currentLevel) {
        if (currentLevel >= 5) return "已达到最高等级";
        return levelToString(currentLevel + 1);
    }

    /**
     * 根据总积分计算当前等级
     * 1级: 0 ~ 499, 2级: 500 ~ 1499, 3级: 1500 ~ 4999, 4级: 5000 ~ 9999, 5级: 10000+
     */
    private int computeLevelByPoints(int points) {
        for (int i = LEVEL_THRESHOLDS.length - 2; i >= 0; i--) {
            if (points >= LEVEL_THRESHOLDS[i]) {
                return i + 1;
            }
        }
        return 1;
    }
}
