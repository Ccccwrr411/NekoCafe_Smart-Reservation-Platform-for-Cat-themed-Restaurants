package cn.edu.bjfu.nekocafe.dto;

import java.util.List;

/**
 * DTO - 提交评价请求体（对应接口 M-1）
 */
public class ReviewSubmitDTO {
    private String orderId;
    private Integer rating;
    private List<String> tags;
    private String content;
    private Integer foodRating;
    private Integer serviceRating;
    private Integer environmentRating;
    private Integer catInteractionRating;

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getFoodRating() { return foodRating; }
    public void setFoodRating(Integer foodRating) { this.foodRating = foodRating; }
    public Integer getServiceRating() { return serviceRating; }
    public void setServiceRating(Integer serviceRating) { this.serviceRating = serviceRating; }
    public Integer getEnvironmentRating() { return environmentRating; }
    public void setEnvironmentRating(Integer environmentRating) { this.environmentRating = environmentRating; }
    public Integer getCatInteractionRating() { return catInteractionRating; }
    public void setCatInteractionRating(Integer catInteractionRating) { this.catInteractionRating = catInteractionRating; }
}
