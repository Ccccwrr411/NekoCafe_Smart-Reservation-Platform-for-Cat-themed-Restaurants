package cn.edu.bjfu.nekocafe.exception;

import cn.edu.bjfu.nekocafe.common.ErrorCode;
import cn.edu.bjfu.nekocafe.common.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 统一捕获所有 Controller 抛出的异常，转为标准 Result 响应
 *
 * 已处理：
 *   - BusinessException  业务异常（如"资源不存在"）
 *   - Exception          兜底处理（500）
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        e.printStackTrace();
        return Result.error(ErrorCode.SERVER_ERROR, "服务器内部错误：" + e.getMessage());
    }
}
