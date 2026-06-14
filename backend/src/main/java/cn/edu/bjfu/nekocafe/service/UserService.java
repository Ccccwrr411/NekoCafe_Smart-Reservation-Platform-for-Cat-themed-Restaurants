package cn.edu.bjfu.nekocafe.service;

import cn.edu.bjfu.nekocafe.dto.RealnameDTO;
import cn.edu.bjfu.nekocafe.dto.UserUpdateDTO;
import cn.edu.bjfu.nekocafe.vo.UserProfileVO;
import java.util.Map;

/**
 * 用户服务接口
 * 实现类：UserServiceImpl
 */
public interface UserService {

    /**
     * 获取用户详细信息（F-1）
     * userId 从 Token 解析，不信任前端传入
     */
    UserProfileVO getProfile(Long userId);

    /**
     * 实名认证（F-2）
     * 返回 verified + realName + idCardMask
     */
    Map<String, Object> verifyRealname(Long userId, RealnameDTO dto);

    /**
     * 更新用户个人信息（昵称/头像/手机/邮箱）
     */
    UserProfileVO updateProfile(Long userId, UserUpdateDTO dto);

    /**
     * 修改密码
     */
    Map<String, Object> changePassword(Long userId, String oldPassword, String newPassword);
}
