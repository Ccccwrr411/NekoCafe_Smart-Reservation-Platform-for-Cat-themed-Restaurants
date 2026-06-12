package cn.edu.bjfu.nekocafe.vo;

import java.util.List;

/**
 * VO - 门店列表响应体（对应接口 B-1）
 * entity Stores → 需要补充 rating / catCount / tags / imageUrl 等
 * （这些字段在 stores 表中暂无，需联查或单独维护）
 */
public class StoreVO {
    private Integer id;          // storeId
    private String name;
    private String address;
    private Double distance;     // 距离 km（可由前端传经纬度后端计算）
    private Double lat;
    private Double lng;
    private Integer avgPrice;
    private Double rating;
    private Integer catCount;
    private String imageUrl;     // 完整 OSS URL，直接来自 stores.image_url 字段
    private List<String> tags;
    private String openTime;     // 来自 businessHours 字段
    private String status;       // 1→"open" / 0→"closed"

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public Double getDistance() { return distance; }
    public void setDistance(Double distance) { this.distance = distance; }
    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }
    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }
    public Integer getAvgPrice() { return avgPrice; }
    public void setAvgPrice(Integer avgPrice) { this.avgPrice = avgPrice; }
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
    public Integer getCatCount() { return catCount; }
    public void setCatCount(Integer catCount) { this.catCount = catCount; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public String getOpenTime() { return openTime; }
    public void setOpenTime(String openTime) { this.openTime = openTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
