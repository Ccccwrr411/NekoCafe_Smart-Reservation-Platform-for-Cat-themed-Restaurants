package cn.edu.bjfu.nekocafe.interceptor;

import cn.edu.bjfu.nekocafe.annotation.RequireRole;
import cn.edu.bjfu.nekocafe.common.ErrorCode;
import cn.edu.bjfu.nekocafe.common.Result;
import cn.edu.bjfu.nekocafe.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

/**
 * JWT 认证 + 角色校验 拦截器
 *
 * 1. 校验 Authorization 头中的 Bearer Token
 * 2. 解析 userId / roleId / storeId 放入 request attribute
 * 3. 检测 @RequireRole 注解，校验当前角色是否在允许列表中
 *
 * 后续 Controller 可通过 request.getAttribute("userId") 获取当前用户
 */
public class AuthInterceptor implements HandlerInterceptor {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        // 1. 取 Authorization 请求头
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeUnauthorized(response, "未登录，请先授权");
            return false;
        }

        // 2. 去掉 "Bearer " 前缀
        String token = authHeader.substring(7);

        // 3. 校验 token 有效性
        if (!JwtUtil.validateToken(token)) {
            writeUnauthorized(response, "Token 无效或已过期，请重新登录");
            return false;
        }

        // 4. 解析用户信息，放入 request attribute
        Long userId = JwtUtil.getUserIdFromToken(token);
        Integer roleId = JwtUtil.getRoleIdFromToken(token);
        Integer storeId = JwtUtil.getStoreIdFromToken(token);

        request.setAttribute("userId", userId);
        request.setAttribute("roleId", roleId);
        request.setAttribute("storeId", storeId);

        // 5. 角色校验：检测 @RequireRole 注解
        if (handler instanceof HandlerMethod) {
            HandlerMethod hm = (HandlerMethod) handler;

            // 先从方法上取，再从类上取
            RequireRole annotation = hm.getMethodAnnotation(RequireRole.class);
            if (annotation == null) {
                annotation = hm.getBeanType().getAnnotation(RequireRole.class);
            }

            if (annotation != null) {
                int[] allowedRoles = annotation.value();
                if (allowedRoles.length > 0) {
                    if (roleId == null) {
                        writeForbidden(response, "旧版 Token 不含角色信息，请重新登录");
                        return false;
                    }
                    boolean matched = false;
                    for (int r : allowedRoles) {
                        if (r == roleId) {
                            matched = true;
                            break;
                        }
                    }
                    if (!matched) {
                        writeForbidden(response, "当前角色无权访问此接口，需要角色ID: " + Arrays.toString(allowedRoles));
                        return false;
                    }
                }
            }
        }

        return true; // 放行
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(MAPPER.writeValueAsString(Result.error(ErrorCode.UNAUTHORIZED, message)));
    }

    private void writeForbidden(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(MAPPER.writeValueAsString(Result.error(ErrorCode.FORBIDDEN, message)));
    }
}
