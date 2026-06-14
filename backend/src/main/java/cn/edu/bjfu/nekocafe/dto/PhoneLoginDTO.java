package cn.edu.bjfu.nekocafe.dto;

/**
 * DTO - 手机号密码登录请求体
 * 接口：POST /api/auth/login/phone
 */
public class PhoneLoginDTO {
    /** 手机号 */
    private String phone;
    /** 密码 */
    private String password;

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
