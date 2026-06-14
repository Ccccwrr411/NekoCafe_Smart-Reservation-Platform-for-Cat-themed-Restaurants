package cn.edu.bjfu.nekocafe.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解（FR23）。
 *
 * 标注在 Controller 方法上，AOP 切面自动记录操作日志到 operation_logs 表。
 *
 * 用法：
 *   @OperationLog("创建优惠活动")
 *   @OperationLog(value = "审核退款", recordParams = true)
 *
 * 记录字段：
 *   - userId / roleId  从 JWT token 获取
 *   - operationType    注解 value 值
 *   - targetType       类名::方法名（自动推断）
 *   - targetId         方法返回值中的 id 字段（自动提取）
 *   - requestParams    请求参数 JSON（仅 recordParams=true 时记录）
 *   - requestIp / userAgent  从 HttpServletRequest 获取
 *   - result           SUCCESS / FAILURE
 *   - errorMsg         异常信息
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    /** 操作描述，如 "创建优惠活动"、"审核退款" */
    String value();

    /** 是否记录请求参数（默认 false，避免敏感信息入库） */
    boolean recordParams() default false;
}
