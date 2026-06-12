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

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
