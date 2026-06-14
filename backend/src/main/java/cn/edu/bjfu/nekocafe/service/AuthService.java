package cn.edu.bjfu.nekocafe.service;

import cn.edu.bjfu.nekocafe.dto.LoginDTO;
import cn.edu.bjfu.nekocafe.dto.PhoneLoginDTO;
import cn.edu.bjfu.nekocafe.dto.RegisterDTO;
import cn.edu.bjfu.nekocafe.vo.LoginVO;

import java.util.Map;

/**
 * 认证服务接口
 * 实现类：AuthServiceImpl
 */
public interface AuthService {

    /**
     * 微信快捷登录（仅用 code 查 openid）
     * 流程：code → 查 users.openid → 找到则签发 JWT，找不到返回错误提示注册
     */
    LoginVO wxQuickLogin(String code);

    /**
     * 微信登录（A-1）
     * 流程：code → 调微信 API 换 openid → 查/创建用户 → 签发 JWT
     */
    LoginVO wxLogin(LoginDTO dto);

    /**
     * 发送验证码（沙箱模式）
     * 生成 6 位随机码，存 Redis（5 分钟过期），课设模式直接返回给前端
     *
     * @param phone 手机号
     * @return 含 code 字段的 map（课设沙箱：验证码直接返回给前端弹窗显示）
     */
    Map<String, Object> sendCode(String phone);

    /**
     * 手机号注册
     * 校验验证码 → 查手机号是否已注册 → BCrypt 加密密码 → 创建用户 → 签发 JWT
     */
    LoginVO register(RegisterDTO dto);

    /**
     * 手机号密码登录
     * 查手机号 → BCrypt 校验密码 → 签发 JWT
     */
    LoginVO phoneLogin(PhoneLoginDTO dto);
}
