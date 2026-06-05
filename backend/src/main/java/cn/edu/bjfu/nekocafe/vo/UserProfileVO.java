package cn.edu.bjfu.nekocafe.vo;

import java.util.List;

/**
 * VO - 用户信息响应体（对应接口 F-1）
 * entity Users + MemberExt 联查组装
 */
public class UserProfileVO {
    private Long id;
    private String nickName;
    private String avatarUrl;
    private String phone;          // 脱敏手机号，如 138****8888
    private String memberLevel;    // MemberExt.level 枚举转文字
    private String memberLevelIcon;// 等级图标 emoji
    private Integer points;        // totalPoints
    private Integer pointsToNext;  // 距下一级所需积分
    private String nextLevel;
    private Integer totalOrders;
    private Integer totalSpent;    // cumulativeAmount 取整
    private Integer couponCount;
    private List<Integer> favoriteStores;
    private String joinDate;       // Users.createdAt 格式化

    // ---- Getters & Setters ----
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNickName() { return nickName; }
    public void setNickName(String nickName) { this.nickName = nickName; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getMemberLevel() { return memberLevel; }
    public void setMemberLevel(String memberLevel) { this.memberLevel = memberLevel; }
    public String getMemberLevelIcon() { return memberLevelIcon; }
    public void setMemberLevelIcon(String memberLevelIcon) { this.memberLevelIcon = memberLevelIcon; }
    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }
    public Integer getPointsToNext() { return pointsToNext; }
    public void setPointsToNext(Integer pointsToNext) { this.pointsToNext = pointsToNext; }
    public String getNextLevel() { return nextLevel; }
    public void setNextLevel(String nextLevel) { this.nextLevel = nextLevel; }
    public Integer getTotalOrders() { return totalOrders; }
    public void setTotalOrders(Integer totalOrders) { this.totalOrders = totalOrders; }
    public Integer getTotalSpent() { return totalSpent; }
    public void setTotalSpent(Integer totalSpent) { this.totalSpent = totalSpent; }
    public Integer getCouponCount() { return couponCount; }
    public void setCouponCount(Integer couponCount) { this.couponCount = couponCount; }
    public List<Integer> getFavoriteStores() { return favoriteStores; }
    public void setFavoriteStores(List<Integer> favoriteStores) { this.favoriteStores = favoriteStores; }
    public String getJoinDate() { return joinDate; }
    public void setJoinDate(String joinDate) { this.joinDate = joinDate; }
}
