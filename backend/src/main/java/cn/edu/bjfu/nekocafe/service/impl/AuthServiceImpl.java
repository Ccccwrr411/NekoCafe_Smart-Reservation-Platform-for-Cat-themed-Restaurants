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

<<<<<<< Updated upstream
            // 同时创建 member_ext 记录
            MemberExt memberExt = new MemberExt();
            memberExt.setUserId(user.getUserId());
            memberExt.setLevel(1);                   // 默认普通会员
            memberExt.setTotalPoints(0);
            memberExt.setCreatedAt(new Date());
            memberExtMapper.insertSelective(memberExt);
=======
        Users user = list.get(0);

        // 检查用户状态
        if (user.getStatus() != null && user.getStatus() != 1) {
            throw new IllegalArgumentException("账号已被禁用，请联系客服");
        }

        return buildLoginVO(user, null);
    }

    // ==================== 微信登录 ====================

    @Override
    public LoginVO wxLogin(LoginDTO dto) {
        String phone = dto.getPhone();
        String smsCode = dto.getSmsCode();

        // 1. 参数校验
        if (phone == null || !phone.matches("^1\\d{10}$")) {
            throw new IllegalArgumentException("手机号格式不正确");
        }
        if (smsCode == null || smsCode.isEmpty()) {
            throw new IllegalArgumentException("验证码不能为空");
        }

        // 2. 校验验证码（从 Redis 取）
        String redisKey = SMS_CODE_PREFIX + phone;
        Object cachedCode = redisTemplate.opsForValue().get(redisKey);
        if (cachedCode == null) {
            throw new IllegalArgumentException("验证码已过期，请重新获取");
        }
        if (!smsCode.equals(cachedCode.toString())) {
            throw new IllegalArgumentException("验证码错误");
        }

        // 3. 用手机号查用户
        UsersExample example = new UsersExample();
        example.createCriteria().andPhoneEqualTo(phone);
        List<Users> list = usersMapper.selectByExample(example);

        if (list.isEmpty()) {
            // 手机号未注册 → 提示用户先注册
            throw new IllegalArgumentException("该手机号未注册，请先注册账号");
        }

        Users user = list.get(0);

        // 4. 检查用户状态
        if (user.getStatus() != null && user.getStatus() != 1) {
            throw new IllegalArgumentException("账号已被禁用，请联系客服");
        }

        // 5. 记录微信 code 到 openid 字段（仅作记录，不影响登录逻辑）
        String wxCode = dto.getCode();
        if (wxCode != null && !wxCode.isEmpty()) {
            Users updateObj = new Users();
            updateObj.setUserId(user.getUserId());
            updateObj.setOpenid(wxCode);
            usersMapper.updateByPrimaryKeySelective(updateObj);
        }

        // 6. 删除已使用的验证码
        redisTemplate.delete(redisKey);

        // 7. 确保有 user_roles 记录（老用户可能缺少关联记录）
        ensureUserRole(user.getUserId(), dto.getRoleId(), dto.getStoreId());

        return buildLoginVO(user, null);
    }

    // ==================== 发送验证码 ====================

    @Override
    public Map<String, Object> sendCode(String phone) {
        // 生成 6 位随机验证码
        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));

        // 存入 Redis，5 分钟过期
        String redisKey = SMS_CODE_PREFIX + phone;
        redisTemplate.opsForValue().set(redisKey, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        // 课设沙箱模式：验证码直接返回给前端弹窗显示
        // 正式环境应调用短信 API 发送，不再返回 code
        Map<String, Object> result = new HashMap<>();
        result.put("code", code);
        result.put("expireMinutes", CODE_EXPIRE_MINUTES);
        return result;
    }

    // ==================== 手机号注册 ====================

    @Override
    public LoginVO register(RegisterDTO dto) {
        String phone = dto.getPhone();
        String password = dto.getPassword();
        String code = dto.getCode();
        String nickname = dto.getNickname();
        String email = dto.getEmail();

        // 1. 参数校验
        if (phone == null || !phone.matches("^1\\d{10}$")) {
            throw new IllegalArgumentException("手机号格式不正确");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("密码长度至少6位");
        }
        if (code == null || code.isEmpty()) {
            throw new IllegalArgumentException("验证码不能为空");
        }
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new IllegalArgumentException("昵称不能为空");
        }

        // 2. 校验验证码（从 Redis 取）
        String redisKey = SMS_CODE_PREFIX + phone;
        Object cachedCode = redisTemplate.opsForValue().get(redisKey);
        if (cachedCode == null) {
            throw new IllegalArgumentException("验证码已过期，请重新获取");
        }
        if (!code.equals(cachedCode.toString())) {
            throw new IllegalArgumentException("验证码错误");
        }

        // 3. 查手机号是否已注册
        UsersExample example = new UsersExample();
        example.createCriteria().andPhoneEqualTo(phone);
        if (!usersMapper.selectByExample(example).isEmpty()) {
            throw new IllegalArgumentException("该手机号已注册，请直接登录");
        }

        // 4. 创建用户
        Users user = new Users();
        user.setPhone(phone);
        user.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt()));    // BCrypt 加密
        user.setNickname(nickname.trim());                                   // 用户自定义昵称
        user.setEmail(email != null && !email.trim().isEmpty() ? email.trim() : null);
        user.setAvatarUrl("/uploads/avatars/default.png");
        user.setStatus((short) 1);
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());
        usersMapper.insertSelective(user);

        // 5. 创建会员积分记录
        MemberExt memberExt = new MemberExt();
        memberExt.setUserId(user.getUserId());
        memberExt.setLevel(1);
        memberExt.setTotalPoints(0);
        memberExt.setCreatedAt(new Date());
        memberExtMapper.insertSelective(memberExt);

        // 6. 写入 user_roles 表
        Integer roleId = dto.getRoleId();
        if (roleId == null) roleId = DEFAULT_ROLE_ID;
        insertUserRole(user.getUserId(), roleId, dto.getStoreId());

        // 7. 删除已使用的验证码
        redisTemplate.delete(redisKey);

        // 8. 签发 JWT，返回
        return buildLoginVO(user, null);
    }

    // ==================== 手机号密码登录 ====================

    @Override
    public LoginVO phoneLogin(PhoneLoginDTO dto) {
        String phone = dto.getPhone();
        String password = dto.getPassword();

        // 1. 参数校验
        if (phone == null || phone.isEmpty()) {
            throw new IllegalArgumentException("手机号不能为空");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }

        // 2. 查手机号
        UsersExample example = new UsersExample();
        example.createCriteria().andPhoneEqualTo(phone);
        List<Users> list = usersMapper.selectByExample(example);
        if (list.isEmpty()) {
            throw new IllegalArgumentException("手机号未注册");
        }

        Users user = list.get(0);

        // 3. 校验密码
        if (user.getPasswordHash() == null || !BCrypt.checkpw(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("密码错误");
        }

        // 4. 检查用户状态
        if (user.getStatus() != null && user.getStatus() != 1) {
            throw new IllegalArgumentException("账号已被禁用，请联系客服");
        }

        // 5. 签发 JWT（传入前端选择的角色）
        return buildLoginVO(user, dto.getRoleId());
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 写入 user_roles 关联表
     * @param userId 用户 ID
     * @param roleId 角色 ID（必填）
     * @param storeId 门店 ID（顾客可为 null）
     */
    private void insertUserRole(Long userId, Integer roleId, Integer storeId) {
        UserRoles ur = new UserRoles();
        ur.setUserId(userId);
        ur.setRoleId(roleId);

        // 总部运营（roleId=4）：storeId 为 null，表示全门店权限
        // 顾客（roleId=1）：storeId 可为 null
        // 其他非顾客角色：优先用传入的 storeId，没传则默认门店 1
        if (roleId != null && roleId == 4) {
            ur.setStoreId(null);  // 全门店权限
        } else if (roleId != null && roleId > 1 && storeId == null) {
            ur.setStoreId(1);  // 课设兜底
>>>>>>> Stashed changes
        } else {
            user = list.get(0);
        }

<<<<<<< Updated upstream
        // ========== 2. 查积分和等级 ==========
=======
        userRolesMapper.insertSelective(ur);
    }

    /**
     * 确保用户有 user_roles 记录；若无则补写入一条
     * 已存在记录时不覆盖，保留原有角色/门店分配
     */
    private void ensureUserRole(Long userId, Integer roleId, Integer storeId) {
        UserRolesExample ure = new UserRolesExample();
        ure.createCriteria().andUserIdEqualTo(userId);
        if (userRolesMapper.countByExample(ure) > 0) {
            return; // 已有记录，保留原分配
        }
        // 无记录，补写入
        Integer rid = roleId != null ? roleId : DEFAULT_ROLE_ID;
        insertUserRole(userId, rid, storeId);
    }

    // ==================== 工具方法 ====================

    /** 构建登录响应 VO（微信登录、手机号注册、手机号登录共用）
     * @param selectedRoleId 前端选择的角色 ID，非登录场景传 null 则取第一条
     */
    private LoginVO buildLoginVO(Users user, Integer selectedRoleId) {
        // 查积分和等级
>>>>>>> Stashed changes
        MemberExt memberExt = memberExtMapper.selectByPrimaryKey(user.getUserId());
        int points = (memberExt != null && memberExt.getTotalPoints() != null)
                ? memberExt.getTotalPoints() : 0;
        int level = (memberExt != null && memberExt.getLevel() != null)
                ? memberExt.getLevel() : 1;

<<<<<<< Updated upstream
        // ========== 3. 签发 JWT ==========
        String token = JwtUtil.generateToken(user.getUserId());
=======
        // 查角色和门店：如果前端指定了角色，优先匹配该角色行；否则取第一条
        UserRolesExample ure = new UserRolesExample();
        ure.createCriteria().andUserIdEqualTo(user.getUserId());
        List<UserRoles> userRoles = userRolesMapper.selectByExample(ure);
        Integer roleId = null;
        Integer storeId = null;
        if (!userRoles.isEmpty()) {
            UserRoles ur = null;
            if (selectedRoleId != null) {
                // 按前端选的角色匹配
                for (UserRoles r : userRoles) {
                    if (selectedRoleId.equals(r.getRoleId())) {
                        ur = r;
                        break;
                    }
                }
            }
            if (ur == null) {
                ur = userRoles.get(0); // 兜底：取第一条
            }
            roleId = ur.getRoleId();
            storeId = ur.getStoreId();
        }

        // 签发 JWT（携带 roleId + storeId，用于拦截器角色校验）
        String token = JwtUtil.generateToken(user.getUserId(), roleId, storeId);
>>>>>>> Stashed changes

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
