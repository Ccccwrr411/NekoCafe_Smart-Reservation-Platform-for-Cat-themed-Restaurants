package cn.edu.bjfu.nekocafe.controller;

import cn.edu.bjfu.nekocafe.common.Result;
import cn.edu.bjfu.nekocafe.dto.RealnameDTO;
import cn.edu.bjfu.nekocafe.dto.UserUpdateDTO;
import cn.edu.bjfu.nekocafe.service.UserService;
import cn.edu.bjfu.nekocafe.vo.UserProfileVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 用户 Controller
 * 接口：F-1 GET /api/user/profile
 *       F-2 POST /api/user/realname
 *       F-3 POST /api/user/profile (更新个人信息)
 *       F-4 POST /api/user/change-password
 *       F-5 POST /api/user/upload-avatar (上传头像)
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    /** F-5 上传头像（MultipartFile） */
    @PostMapping("/upload-avatar")
    public Result<Map<String, Object>> uploadAvatar(@RequestParam("file") MultipartFile file,
                                                     HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (file.isEmpty()) {
            return Result.error(400, "请选择要上传的图片");
        }
        // 限制文件类型和大小 (2MB)
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg")
                && !contentType.equals("image/png")
                && !contentType.equals("image/webp"))) {
            return Result.error(400, "仅支持 JPG/PNG/WebP 格式");
        }
        if (file.getSize() > 2 * 1024 * 1024) {
            return Result.error(400, "图片大小不能超过 2MB");
        }

        // 确定保存路径：uploads/avatars/
        String uploadDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator + "avatars";
        new File(uploadDir).mkdirs();

        // 文件名: {userId}_{UUID}.{ext}
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        } else if (contentType.contains("png")) {
            ext = ".png";
        } else if (contentType.contains("webp")) {
            ext = ".webp";
        } else {
            ext = ".jpg";
        }
        String fileName = userId + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8) + ext;

        try {
            File destFile = new File(uploadDir, fileName);
            file.transferTo(destFile);
        } catch (IOException e) {
            return Result.error(500, "文件保存失败: " + e.getMessage());
        }

        // 返回可访问的 URL 路径
        String avatarUrl = "/uploads/avatars/" + fileName;

        // 同步更新用户头像
        userService.updateProfile(userId, new UserUpdateDTO(null, avatarUrl, null, null));

        Map<String, Object> result = new HashMap<>();
        result.put("avatarUrl", avatarUrl);
        result.put("success", true);
        return Result.success(result);
    }

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

    /** F-3 更新个人信息（昵称/头像/手机/邮箱） */
    @PostMapping("/profile")
    public Result<UserProfileVO> updateProfile(@RequestBody UserUpdateDTO dto,
                                                HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(userService.updateProfile(userId, dto));
    }

    /** F-4 修改密码 */
    @PostMapping("/change-password")
    public Result<Map<String, Object>> changePassword(@RequestBody Map<String, String> body,
                                                       HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        return Result.success(userService.changePassword(userId, oldPassword, newPassword));
    }
}
