package cn.edu.bjfu.nekocafe.entity;

import java.math.BigDecimal;
import java.util.Date;

public class CouponUsage {
    private Long usageId;

    private Long couponId;

    private Long reservationId;

    private BigDecimal discountAmount;

    private Date usedAt;

    public Long getUsageId() {
        return usageId;
    }

    public void setUsageId(Long usageId) {
        this.usageId = usageId;
    }

    public Long getCouponId() {
        return couponId;
    }

    public void setCouponId(Long couponId) {
        this.couponId = couponId;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public Date getUsedAt() {
        return usedAt;
    }

    public void setUsedAt(Date usedAt) {
        this.usedAt = usedAt;
    }
}