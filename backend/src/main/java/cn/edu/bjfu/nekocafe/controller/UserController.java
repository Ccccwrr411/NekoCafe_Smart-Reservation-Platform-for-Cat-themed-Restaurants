package cn.edu.bjfu.nekocafe.controller;

import cn.edu.bjfu.nekocafe.common.Result;
import cn.edu.bjfu.nekocafe.dto.RealnameDTO;
import cn.edu.bjfu.nekocafe.service.UserService;
import cn.edu.bjfu.nekocafe.vo.UserProfileVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * 用户 Controller
 * 负责人：___
 * 接口：F-1 GET /api/user/profile
 *       F-2 POST /api/user/realname
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    /** F-1 用户信息 */
    @GetMapping("/profile")
    public Result<UserProfileVO> getProfile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(userService.getProfile(userId));
    }

    /** F-2 实名认证 */
    @PostMapping("/realname")
    public Result<Map<String, Object>> verifyRealname(@RequestBody RealnameDTO dto,
                                                       HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(userService.verifyRealname(userId, dto));
    }
}
