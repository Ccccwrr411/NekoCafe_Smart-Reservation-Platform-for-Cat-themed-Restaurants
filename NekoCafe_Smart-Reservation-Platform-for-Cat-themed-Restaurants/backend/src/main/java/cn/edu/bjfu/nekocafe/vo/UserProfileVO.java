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
    private String phone;          // 脱敏手机号，如 138****8888（用户绑定后才有值）
    private String email;          // 邮箱（用户绑定后才有值）
    private String memberLevel;    // MemberExt.level 枚举转文字
    private String memberLevelIcon;// 等级图标 emoji
    private Integer points;        // totalPoints
    private Integer pointsToNext;  // 距下一级所需积分
    private String nextLevel;
    private Integer levelStartPoints;   // 当前等级起始积分（进度条起点）
    private Integer nextLevelThreshold; // 下一级积分阈值（进度条终点）
    private Integer totalOrders;
    private Integer totalSpent;    // cumulativeAmount 取整
    private Integer couponCount;
    private List<Integer> favoriteStores;
    private String joinDate;       // Users.createdAt 格式化
    private Boolean isVerified;    // 是否已完成实名认证
    private String realName;       // 实名认证姓名（未认证为 null）
    private String idCardMask;     // 脱敏身份证号（未认证为 null）
    private Integer storeId;       // 用户绑定的门店 ID
    private String storeName;      // 用户绑定的门店名称

    // ---- Getters & Setters ----
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNickName() { return nickName; }
    public void setNickName(String nickName) { this.nickName = nickName; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
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
    public Integer getLevelStartPoints() { return levelStartPoints; }
    public void setLevelStartPoints(Integer levelStartPoints) { this.levelStartPoints = levelStartPoints; }
    public Integer getNextLevelThreshold() { return nextLevelThreshold; }
    public void setNextLevelThreshold(Integer nextLevelThreshold) { this.nextLevelThreshold = nextLevelThreshold; }
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
    public Boolean getIsVerified() { return isVerified; }
    public void setIsVerified(Boolean isVerified) { this.isVerified = isVerified; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getIdCardMask() { return idCardMask; }
    public void setIdCardMask(String idCardMask) { this.idCardMask = idCardMask; }
    public Integer getStoreId() { return storeId; }
    public void setStoreId(Integer storeId) { this.storeId = storeId; }
    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }
}
