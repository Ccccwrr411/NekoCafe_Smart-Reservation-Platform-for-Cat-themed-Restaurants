package cn.edu.bjfu.nekocafe.entity;

import java.util.Date;

public class UserCoupons {
    private Long couponId;

    private Long userId;

    private Integer promoId;

    private String status;

    private Date usedAt;

    private Long usedReservationId;

    private Date expireTime;

    private Date createdAt;

    public Long getCouponId() {
        return couponId;
    }

    public void setCouponId(Long couponId) {
        this.couponId = couponId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getPromoId() {
        return promoId;
    }

    public void setPromoId(Integer promoId) {
        this.promoId = promoId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getUsedAt() {
        return usedAt;
    }

    public void setUsedAt(Date usedAt) {
        this.usedAt = usedAt;
    }

    public Long getUsedReservationId() {
        return usedReservationId;
    }

    public void setUsedReservationId(Long usedReservationId) {
        this.usedReservationId = usedReservationId;
    }

    public Date getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Date expireTime) {
        this.expireTime = expireTime;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}