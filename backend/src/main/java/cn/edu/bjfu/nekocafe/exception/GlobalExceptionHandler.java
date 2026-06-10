package cn.edu.bjfu.nekocafe.exception;

import cn.edu.bjfu.nekocafe.common.ErrorCode;
import cn.edu.bjfu.nekocafe.common.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理器
 * 统一捕获所有 Controller 抛出的异常，转为标准 Result 响应
 *
 * 已处理：
 *   - BusinessException       业务异常（如"资源不存在"）
 *   - NoResourceFoundException 静态资源 → 返回 404（不转 500）
 *   - Exception               兜底处理（500）
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

    /** Spring 找不到静态资源时抛出的异常，直接返回 404 即可 */
    @ExceptionHandler(NoResourceFoundException.class)
    public Result<?> handleNoResource(NoResourceFoundException e) {
        return Result.error(404, "资源不存在：" + e.getResourcePath());
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        e.printStackTrace();
        return Result.error(ErrorCode.SERVER_ERROR, "服务器内部错误：" + e.getMessage());
    }
}
