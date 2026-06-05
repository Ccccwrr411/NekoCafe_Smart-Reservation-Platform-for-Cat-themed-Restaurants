package cn.edu.bjfu.nekocafe.dto;

/**
 * DTO - 微信登录请求体（对应接口 A-1）
 */
public class LoginDTO {
    /** wx.login() 返回的临时 code */
    private String code;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
