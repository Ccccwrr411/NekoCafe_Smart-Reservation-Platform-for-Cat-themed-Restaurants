package cn.edu.bjfu.nekocafe.service.impl;

import cn.edu.bjfu.nekocafe.dto.LoginDTO;
import cn.edu.bjfu.nekocafe.entity.MemberExt;
import cn.edu.bjfu.nekocafe.entity.Users;
import cn.edu.bjfu.nekocafe.entity.UsersExample;
import cn.edu.bjfu.nekocafe.mapper.MemberExtMapper;
import cn.edu.bjfu.nekocafe.mapper.UsersMapper;
import cn.edu.bjfu.nekocafe.service.AuthService;
import cn.edu.bjfu.nekocafe.util.JwtUtil;
import cn.edu.bjfu.nekocafe.vo.LoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 认证服务实现
 *
 * 登录流程（课设版）：
 *   1. 前端传 wx.login() 返回的 code
 *   2. 【正式环境】应调微信 code2session 接口换 openid
 *      【课设环境】直接用 code 作为用户标识，存到 phone 字段做查询
 *   3. 根据 phone 查 users 表，若无则自动注册新用户
 *   4. 查 member_ext 表获取积分和等级
 *   5. 调用 JwtUtil.generateToken(userId) 签发 Token
 *   6. 组装 LoginVO 返回
 */
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UsersMapper usersMapper;

    @Autowired
    private MemberExtMapper memberExtMapper;

    @Override
    public LoginVO wxLogin(LoginDTO dto) {
        String code = dto.getCode();
        if (code == null || code.isEmpty()) {
            throw new IllegalArgumentException("code 不能为空");
        }

        // ========== 1. 课设版：用 code 当用户标识查 openid 字段 ==========
        // 正式环境应改为：调微信 code2session 拿真实 openid，然后 phone 字段存真实手机号
        // 注意：微信 code 长度约 32 字符，超过 phone 字段 varchar(20) 限制，必须存 openid 字段
        UsersExample example = new UsersExample();
        example.createCriteria().andOpenidEqualTo(code);
        List<Users> list = usersMapper.selectByExample(example);

        Users user;
        if (list.isEmpty()) {
            // 新用户：自动注册
            user = new Users();
            user.setOpenid(code);                   // 课设用 code 存 openid 字段（varchar 足够长）
            user.setNickname("猫咖爱好者");           // 默认昵称
            user.setAvatarUrl("https://nekocafe-images.oss-cn-beijing.aliyuncs.com/uploads/avatars/default.png");
            user.setStatus((short) 1);              // 1=正常
            user.setCreatedAt(new Date());
            user.setUpdatedAt(new Date());
            usersMapper.insertSelective(user);

            // 同时创建 member_ext 记录
            MemberExt memberExt = new MemberExt();
            memberExt.setUserId(user.getUserId());
            memberExt.setLevel(1);                   // 默认普通会员
            memberExt.setTotalPoints(0);
            memberExt.setCreatedAt(new Date());
            memberExtMapper.insertSelective(memberExt);
        } else {
            user = list.get(0);
        }

        // ========== 2. 查积分和等级 ==========
        MemberExt memberExt = memberExtMapper.selectByPrimaryKey(user.getUserId());
        int points = (memberExt != null && memberExt.getTotalPoints() != null)
                ? memberExt.getTotalPoints() : 0;
        int level = (memberExt != null && memberExt.getLevel() != null)
                ? memberExt.getLevel() : 1;

        // ========== 3. 签发 JWT ==========
        String token = JwtUtil.generateToken(user.getUserId());

        // ========== 4. 组装响应 ==========
        LoginVO result = new LoginVO();
        result.setToken(token);

        LoginVO.UserInfoVO userInfo = new LoginVO.UserInfoVO();
        userInfo.setId(user.getUserId());
        userInfo.setNickName(user.getNickname());
        userInfo.setAvatarUrl(user.getAvatarUrl());
        userInfo.setPhone(maskPhone(user.getPhone()));       // 手机号脱敏（phone 为 null 时返回 null，前端可做判断）
        userInfo.setMemberLevel(levelToString(level));
        userInfo.setPoints(points);
        // 根据前端传入的角色设置权限
        String loginRole = dto.getRole();
        if (loginRole == null || loginRole.isEmpty()) {
            loginRole = "customer";
        }
        userInfo.setRole(loginRole);
        userInfo.setRoleLabel(resolveRoleLabel(loginRole));
        result.setUserInfo(userInfo);

        return result;
    }

    // ========== 工具方法 ==========

    /** 角色英文 → 中文 */
    private String resolveRoleLabel(String role) {
        if (role == null) return "顾客";
        switch (role) {
            case "staff":       return "店员";
            case "manager":     return "店长";
            case "hq_ops":      return "总部运营";
            case "cat_keeper":  return "猫咪管家";
            default:            return "顾客";
        }
    }

    /** 手机号脱敏：保留前3后4，中间变 **** */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return phone;
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    /** 等级数字 → 中文 */
    private String levelToString(int level) {
        switch (level) {
            case 2: return "银卡会员";
            case 3: return "金卡会员";
            case 4: return "黑卡会员";
            default: return "普通会员";
        }
    }
}
