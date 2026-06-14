package cn.edu.bjfu.nekocafe.dto;

/**
 * DTO - 微信登录请求体（对应接口 A-1）
 *
 * 课设版微信登录流程：
 *   前端获取微信手机号授权 → 用手机号+验证码查询用户 → 找到则登录，找不到则提示注册
 */
public class LoginDTO {
    /** wx.login() 返回的临时 code（仅作记录，不参与查询逻辑） */
    private String code;
    /** 手机号（微信授权获取或用户手动输入） */
    private String phone;
    /** 短信验证码（微信登录时用于身份验证） */
    private String smsCode;
    /** 角色 ID（前端选择的角色，对应 roles 表） */
    private Integer roleId;
    /** 门店 ID（非顾客角色必填） */
    private Integer storeId;
    /** 昵称（微信登录新用户可自定义） */
    private String nickname;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getSmsCode() { return smsCode; }
    public void setSmsCode(String smsCode) { this.smsCode = smsCode; }
    public Integer getRoleId() { return roleId; }
    public void setRoleId(Integer roleId) { this.roleId = roleId; }
    public Integer getStoreId() { return storeId; }
    public void setStoreId(Integer storeId) { this.storeId = storeId; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
}
