package cn.edu.bjfu.nekocafe.dto;

/**
 * DTO - 微信登录请求体（对应接口 A-1）
 */
public class LoginDTO {
    /** wx.login() 返回的临时 code */
    private String code;
    /** 角色 ID（前端选择的角色，对应 roles 表） */
    private Integer roleId;
    /** 门店 ID（非顾客角色必填） */
    private Integer storeId;
    /** 昵称（微信登录新用户可自定义） */
    private String nickname;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public Integer getRoleId() { return roleId; }
    public void setRoleId(Integer roleId) { this.roleId = roleId; }
    public Integer getStoreId() { return storeId; }
    public void setStoreId(Integer storeId) { this.storeId = storeId; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
}
