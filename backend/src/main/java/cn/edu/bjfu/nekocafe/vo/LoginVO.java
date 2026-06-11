package cn.edu.bjfu.nekocafe.vo;

/**
 * VO - 登录响应体（对应接口 A-1）
 */
public class LoginVO {
    private String token;
    private UserInfoVO userInfo;

    public static class UserInfoVO {
        private Long id;
        private String nickName;
        private String avatarUrl;
        private String phone;
        private String memberLevel;
        private Integer points;

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
        public Integer getPoints() { return points; }
        public void setPoints(Integer points) { this.points = points; }
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public UserInfoVO getUserInfo() { return userInfo; }
    public void setUserInfo(UserInfoVO userInfo) { this.userInfo = userInfo; }
}
