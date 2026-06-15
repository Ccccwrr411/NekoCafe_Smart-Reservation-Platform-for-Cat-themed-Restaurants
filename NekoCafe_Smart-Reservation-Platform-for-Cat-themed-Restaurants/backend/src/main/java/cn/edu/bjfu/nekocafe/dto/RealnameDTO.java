package cn.edu.bjfu.nekocafe.dto;

/**
 * DTO - 实名认证请求体（对应接口 F-2）
 */
public class RealnameDTO {
    private String realName;
    private String idCard;

    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getIdCard() { return idCard; }
    public void setIdCard(String idCard) { this.idCard = idCard; }
}
