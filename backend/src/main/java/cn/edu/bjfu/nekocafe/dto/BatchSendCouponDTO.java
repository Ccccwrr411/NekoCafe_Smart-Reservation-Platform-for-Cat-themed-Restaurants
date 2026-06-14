package cn.edu.bjfu.nekocafe.dto;

import java.util.Date;
import java.util.List;

/**
 * 总部运营 · 批量发券 DTO
 *
 * targetType 取值：
 *   "user"         → 指定 userId 列表
 *   "role"         → 所有 user_roles 对应用户（targetValue = role_id 字符串，如 "1"=顾客）
 *   "member_level" → 所有 member_ext.level >= targetValue 的用户（targetValue = "1"/"2"/"3"）
 *   "all"          → 所有活跃用户（无需 targetValue）
 */
public class BatchSendCouponDTO {

    private Integer promoId;
    private String targetType;
    private String targetValue;
    private List<Long> userIds;
    private Date expireTime;

    public Integer getPromoId() { return promoId; }
    public void setPromoId(Integer promoId) { this.promoId = promoId; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public String getTargetValue() { return targetValue; }
    public void setTargetValue(String targetValue) { this.targetValue = targetValue; }
    public List<Long> getUsersIds() { return userIds; }
    public void setUsersIds(List<Long> userIds) { this.userIds = userIds; }
    public Date getExpireTime() { return expireTime; }
    public void setExpireTime(Date expireTime) { this.expireTime = expireTime; }
}
