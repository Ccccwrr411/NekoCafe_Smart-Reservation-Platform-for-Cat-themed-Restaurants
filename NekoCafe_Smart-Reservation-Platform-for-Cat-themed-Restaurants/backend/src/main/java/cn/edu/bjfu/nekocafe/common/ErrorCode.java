package cn.edu.bjfu.nekocafe.common;

/**
 * 统一错误码常量
 * 与前端接口契约 1.3 节对应
 */
public class ErrorCode {
    public static final int SUCCESS    = 0;
    public static final int UNAUTHORIZED = 401;  // 未登录 / Token 过期
    public static final int FORBIDDEN  = 403;    // 无权限
    public static final int NOT_FOUND  = 404;    // 资源不存在
    public static final int BAD_REQUEST = 400;   // 参数错误
    public static final int SERVER_ERROR = 500;  // 服务器内部错误
}
