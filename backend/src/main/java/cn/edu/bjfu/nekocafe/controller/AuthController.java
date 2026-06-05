package cn.edu.bjfu.nekocafe.controller;

import cn.edu.bjfu.nekocafe.common.Result;
import cn.edu.bjfu.nekocafe.dto.LoginDTO;
import cn.edu.bjfu.nekocafe.service.AuthService;
import cn.edu.bjfu.nekocafe.vo.LoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 认证 Controller
 * 负责人：___
 * 接口：A-1 POST /api/auth/login
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
}
