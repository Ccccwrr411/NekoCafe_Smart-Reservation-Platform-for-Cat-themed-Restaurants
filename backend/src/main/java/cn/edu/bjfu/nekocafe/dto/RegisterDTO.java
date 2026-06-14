package cn.edu.bjfu.nekocafe.dto;

/**
 * DTO - 手机号注册请求体
 * 接口：POST /api/auth/register
 */
public class RegisterDTO {
    /** 手机号 */
    private String phone;
    /** 密码（明文，后端 BCrypt 加密后存储） */
    private String password;
    /** 短信验证码（沙箱模式：前端直接显示，用户手填） */
    private String code;
    /** 昵称 */
    private String nickname;
    /** 邮箱 */
    private String email;
    /** 角色 ID（对应 roles 表：1=顾客, 2=店员, 3=店长, 4=总部运营, 5=猫咪管家），默认 1=顾客 */
    private Integer roleId;
    /** 门店 ID（非顾客角色必填，顾客可为空） */
    private Integer storeId;

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Integer getRoleId() { return roleId; }
    public void setRoleId(Integer roleId) { this.roleId = roleId; }
    public Integer getStoreId() { return storeId; }
    public void setStoreId(Integer storeId) { this.storeId = storeId; }
}
