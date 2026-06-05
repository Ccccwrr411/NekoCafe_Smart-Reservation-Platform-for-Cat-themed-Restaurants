package cn.edu.bjfu.nekocafe.service;

import cn.edu.bjfu.nekocafe.dto.LoginDTO;
import cn.edu.bjfu.nekocafe.vo.LoginVO;

/**
 * 认证服务接口
 * 实现类：AuthServiceImpl
 */
public interface AuthService {

    /**
     * 微信登录
     * 流程：code → 调微信 API 换 openid → 查/创建用户 → 签发 JWT
     *
     * @param dto 含 wx.login() 返回的 code
     * @return token + userInfo
     */
    LoginVO wxLogin(LoginDTO dto);
}
