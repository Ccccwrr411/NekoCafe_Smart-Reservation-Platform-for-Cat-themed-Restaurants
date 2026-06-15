package cn.edu.bjfu.nekocafe.dto;

import java.util.Date;

/**
 * 总部运营 · 给指定用户发券 DTO
 */
public class SendCouponDTO {

    private Integer promoId;
    private Long userId;
    private Date expireTime;          // 过期时间（不传则用活动结束时间）

    public Integer getPromoId() { return promoId; }
    public void setPromoId(Integer promoId) { this.promoId = promoId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Date getExpireTime() { return expireTime; }
    public void setExpireTime(Date expireTime) { this.expireTime = expireTime; }
}
