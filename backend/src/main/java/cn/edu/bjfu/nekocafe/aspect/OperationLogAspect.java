package cn.edu.bjfu.nekocafe.aspect;

import cn.edu.bjfu.nekocafe.annotation.OperationLog;
import cn.edu.bjfu.nekocafe.entity.OperationLogs;
import cn.edu.bjfu.nekocafe.mapper.OperationLogsMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Date;

/**
 * 操作日志 AOP 切面（FR23 操作日志）
 *
 * 拦截所有标注了 @OperationLog 的 Controller 方法，
 * 自动记录操作人、操作类型、请求参数、执行结果等到 operation_logs 表。
 */
@Aspect
@Component
public class OperationLogAspect {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private OperationLogsMapper operationLogsMapper;

    @Around("@annotation(cn.edu.bjfu.nekocafe.annotation.OperationLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        // 提取注解信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        OperationLog annotation = method.getAnnotation(OperationLog.class);

        // 构建日志实体
        OperationLogs log = new OperationLogs();
        log.setOperationType(annotation.value());
        log.setTargetType(joinPoint.getTarget().getClass().getSimpleName() + "." + method.getName());
        log.setCreatedAt(new Date());

        // 从 HttpServletRequest 提取上下文
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            Object uid = request.getAttribute("userId");
            Object rid = request.getAttribute("roleId");

            if (uid instanceof Long) log.setUserId((Long) uid);
            if (rid instanceof Integer) {
                log.setRoleAtTime(roleIdToName((Integer) rid));
            }
            log.setRequestIp(getClientIp(request));
            String ua = request.getHeader("User-Agent");
            log.setUserAgent(ua != null && ua.length() > 255 ? ua.substring(0, 255) : ua);
        }

        // 记录参数
        if (annotation.recordParams()) {
            try {
                Object[] args = joinPoint.getArgs();
                // 过滤掉 HttpServletRequest / HttpServletResponse 等不可序列化对象
                Object[] cleanArgs = new Object[args.length];
                for (int i = 0; i < args.length; i++) {
                    if (args[i] instanceof jakarta.servlet.http.HttpServletRequest
                            || args[i] instanceof jakarta.servlet.http.HttpServletResponse) {
                        cleanArgs[i] = "[ServletRequest]";
                    } else {
                        cleanArgs[i] = args[i];
                    }
                }
                log.setRequestParams(MAPPER.writeValueAsString(cleanArgs));
            } catch (Exception ignored) {
                log.setRequestParams("[序列化失败]");
            }
        }

        // 执行目标方法
        try {
            Object result = joinPoint.proceed();
            log.setResult("SUCCESS");

            // 尝试从返回值中提取 targetId
            if (result != null && log.getTargetId() == null) {
                try {
                    log.setTargetId(extractIdFromResult(result));
                } catch (Exception ignored) {}
            }

            return result;
        } catch (Throwable t) {
            log.setResult("FAILURE");
            String msg = t.getMessage();
            log.setErrorMsg(msg != null && msg.length() > 1000 ? msg.substring(0, 1000) : msg);
            throw t; // 原样抛出，不吞异常
        } finally {
            // 异步写入（这里用同步，课设场景量小无性能问题）
            try {
                operationLogsMapper.insertSelective(log);
            } catch (Exception e) {
                // 日志写入失败不能影响业务，仅打堆栈
                e.printStackTrace();
            }
        }
    }

    /** 角色 ID → 中文名 */
    private String roleIdToName(int roleId) {
        switch (roleId) {
            case 1: return "顾客";
            case 2: return "店员";
            case 3: return "店长";
            case 4: return "总部运营";
            case 5: return "猫咪管家";
            default: return "未知(" + roleId + ")";
        }
    }

    /** 获取客户端真实 IP（考虑反向代理） */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /** 从返回值中提取 id（支持 Result / Map / Entity） */
    private String extractIdFromResult(Object result) throws Exception {
        // 1. Result<T> 对象 — 取 data
        Object data = result;
        if (result.getClass().getSimpleName().equals("Result")) {
            try {
                Method getData = result.getClass().getMethod("getData");
                data = getData.invoke(result);
            } catch (Exception ignored) {}
        }

        if (data == null) return null;

        // 2. Map — 取 id / promoId / couponId / orderId 等
        if (data instanceof java.util.Map) {
            java.util.Map<?, ?> map = (java.util.Map<?, ?>) data;
            for (String key : new String[]{"promoId", "couponId", "id", "orderId", "refundId", "logId"}) {
                Object val = map.get(key);
                if (val != null) return val.toString();
            }
        }

        // 3. 通用实体 — 反射 getXxxId() / getId()
        try {
            for (String methodName : new String[]{"getPromoId", "getCouponId", "getId", "getOrderId", "getRefundId"}) {
                try {
                    Method m = data.getClass().getMethod(methodName);
                    Object val = m.invoke(data);
                    if (val != null) return val.toString();
                } catch (NoSuchMethodException ignored) {}
            }
        } catch (Exception ignored) {}

        return null;
    }
}
