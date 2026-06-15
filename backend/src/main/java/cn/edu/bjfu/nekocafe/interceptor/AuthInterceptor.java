package cn.edu.bjfu.nekocafe.interceptor;

import cn.edu.bjfu.nekocafe.common.ErrorCode;
import cn.edu.bjfu.nekocafe.common.Result;
import cn.edu.bjfu.nekocafe.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 认证拦截器
 *
 * 拦截所有 /api/** 请求（除 /api/auth/login）
 * 校验 Authorization 头中的 Bearer Token，解析出 userId 放入 request attribute
 *
 * 后续 Controller 通过 request.getAttribute("userId") 获取当前用户 ID
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
            writeError(response, ErrorCode.UNAUTHORIZED, "未登录，请先授权");
            return false;
        }

        // 2. 去掉 "Bearer " 前缀，取纯 token
        String token = authHeader.substring(7);

        // 3. 校验 token 有效性
        if (!JwtUtil.validateToken(token)) {
            writeError(response, ErrorCode.UNAUTHORIZED, "Token 无效或已过期，请重新登录");
            return false;
        }

        // 4. 解析 userId，放入 request 属性，后续 Controller 可读取
        Long userId = JwtUtil.getUserIdFromToken(token);
        request.setAttribute("userId", userId);

        return true; // 放行
    }

    /** 向前端返回 JSON 格式的 401 错误 */
    private void writeError(HttpServletResponse response, int code, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        response.getWriter().write(MAPPER.writeValueAsString(Result.error(code, message)));
    }
}
