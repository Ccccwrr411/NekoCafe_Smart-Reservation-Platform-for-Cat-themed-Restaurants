package cn.edu.bjfu.nekocafe.dto;

/**
 * DTO - 微信登录请求体（对应接口 A-1）
 */
public class LoginDTO {
    /** wx.login() 返回的临时 code */
    private String code;

    /** 前端选择的角色：customer / staff / manager / hq_ops / cat_keeper */
    private String role;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
