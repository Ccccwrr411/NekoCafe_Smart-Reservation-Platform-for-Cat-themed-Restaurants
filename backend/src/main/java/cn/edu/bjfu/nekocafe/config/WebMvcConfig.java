package cn.edu.bjfu.nekocafe.config;

import cn.edu.bjfu.nekocafe.interceptor.AuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

/**
 * Spring MVC 全局配置
 * 职责：
 *   1. 注册 JWT 认证拦截器（AuthInterceptor）
 *   2. 配置跨域（CORS）
 *   3. 映射静态资源（uploads/ → /uploads/**）
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor())
                .addPathPatterns("/api/**")               // 拦截所有 /api 接口
                .excludePathPatterns("/api/auth/login");  // 登录接口放行
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // TODO：上线前替换 * 为真实前端域名
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将 /uploads/** 映射到 classpath:/static/uploads/（开发环境）
        // 生产环境改为服务器磁盘路径，如 file:/data/nekocafe/uploads/
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("classpath:/static/uploads/");
    }
}
