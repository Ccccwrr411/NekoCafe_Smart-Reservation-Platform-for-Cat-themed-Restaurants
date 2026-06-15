package cn.edu.bjfu.nekocafe.exception;

/**
 * 业务异常
 * 当业务逻辑失败时抛出，如资源不存在、权限不足等
 *
 * 示例：
 *   throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在");
 */
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
