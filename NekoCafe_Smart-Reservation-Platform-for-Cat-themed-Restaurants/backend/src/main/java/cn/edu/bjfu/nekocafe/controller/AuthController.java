package cn.edu.bjfu.nekocafe.controller;

import cn.edu.bjfu.nekocafe.common.Result;
import cn.edu.bjfu.nekocafe.dto.LoginDTO;
import cn.edu.bjfu.nekocafe.dto.PhoneLoginDTO;
import cn.edu.bjfu.nekocafe.dto.RegisterDTO;
import cn.edu.bjfu.nekocafe.service.AuthService;
import cn.edu.bjfu.nekocafe.vo.LoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证 Controller
 * 接口：
 *   A-1  POST /api/auth/login          微信登录
 *   A-2  POST /api/auth/send-code      发送验证码（沙箱）
 *   A-3  POST /api/auth/register       手机号注册
 *   A-4  POST /api/auth/login/phone    手机号密码登录
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /** A-1 微信登录（无需 Token） */
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginDTO dto) {
        return Result.success(authService.wxLogin(dto));
    }

    /** A-0 微信快捷登录（仅用 wx.login code 查 openid 登录） */
    @PostMapping("/wx-login")
    public Result<LoginVO> wxQuickLogin(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        if (code == null || code.isEmpty()) {
            return Result.error(400, "微信授权code不能为空");
        }
        return Result.success(authService.wxQuickLogin(code));
    }

    /** A-2 发送验证码（沙箱模式，验证码直接返回前端） */
    @PostMapping("/send-code")
    public Result<?> sendCode(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        if (phone == null || phone.isEmpty()) {
            return Result.error(400, "手机号不能为空");
        }
        if (!phone.matches("^1\\d{10}$")) {
            return Result.error(400, "手机号格式不正确");
        }
        return Result.success(authService.sendCode(phone));
    }

    /** A-3 手机号注册 */
    @PostMapping("/register")
    public Result<LoginVO> register(@RequestBody RegisterDTO dto) {
        return Result.success(authService.register(dto));
    }

    /** A-4 手机号密码登录 */
    @PostMapping("/login/phone")
    public Result<LoginVO> phoneLogin(@RequestBody PhoneLoginDTO dto) {
        return Result.success(authService.phoneLogin(dto));
    }
}
