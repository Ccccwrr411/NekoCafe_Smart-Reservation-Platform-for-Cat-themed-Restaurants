package cn.edu.bjfu.nekocafe.entity;

import java.util.Date;

public class Reviews {
    private Long reviewId;

    private Long reservationId;

    private Long userId;

    private Integer storeId;

    private Integer overallRating;

    private Integer foodRating;

    private Integer serviceRating;

    private Integer environmentRating;

    private Integer catInteractionRating;

    private String content;

    private Object images;

    private String reply;

    private Date replyAt;

    private String status;

    private Date createdAt;

    private Date updatedAt;

    public Long getReviewId() {
        return reviewId;
    }

    public void setReviewId(Long reviewId) {
        this.reviewId = reviewId;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getStoreId() {
        return storeId;
    }

    public void setStoreId(Integer storeId) {
        this.storeId = storeId;
    }

    public Integer getOverallRating() {
        return overallRating;
    }

    public void setOverallRating(Integer overallRating) {
        this.overallRating = overallRating;
    }

    public Integer getFoodRating() {
        return foodRating;
    }

    public void setFoodRating(Integer foodRating) {
        this.foodRating = foodRating;
    }

    public Integer getServiceRating() {
        return serviceRating;
    }

    public void setServiceRating(Integer serviceRating) {
        this.serviceRating = serviceRating;
    }

    public Integer getEnvironmentRating() {
        return environmentRating;
    }

    public void setEnvironmentRating(Integer environmentRating) {
        this.environmentRating = environmentRating;
    }

    public Integer getCatInteractionRating() {
        return catInteractionRating;
    }

    public void setCatInteractionRating(Integer catInteractionRating) {
        this.catInteractionRating = catInteractionRating;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Object getImages() {
        return images;
    }

    public void setImages(Object images) {
        this.images = images;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public Date getReplyAt() {
        return replyAt;
    }

    public void setReplyAt(Date replyAt) {
        this.replyAt = replyAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}