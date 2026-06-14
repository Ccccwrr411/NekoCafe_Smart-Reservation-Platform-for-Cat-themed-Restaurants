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
    /** 前端选择的角色 ID（1=顾客, 2=店员, 3=店长, 4=总部运营, 5=猫咪管家） */
    private Integer roleId;
    /** 前端选择的门店 ID */
    private Integer storeId;

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Integer getRoleId() { return roleId; }
    public void setRoleId(Integer roleId) { this.roleId = roleId; }
    public Integer getStoreId() { return storeId; }
    public void setStoreId(Integer storeId) { this.storeId = storeId; }
}
