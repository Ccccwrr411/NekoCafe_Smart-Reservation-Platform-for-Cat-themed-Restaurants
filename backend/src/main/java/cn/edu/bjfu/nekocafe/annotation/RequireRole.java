package cn.edu.bjfu.nekocafe.annotation;

import java.lang.annotation.*;

/**
 * 角色权限校验注解。
 *
 * 用法：
 *   @RequireRole({2, 3})    — 仅允许店员(2)和店长(3)访问
 *   @RequireRole(4)         — 仅允许总部运营(4)访问
 *
 * 角色 ID 映射（与 AuthServiceImpl 一致）：
 *   1=顾客, 2=店员, 3=店长, 4=总部运营, 5=猫咪管家
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {

    /**
     * 允许访问的角色 ID 列表。
     * 空数组表示不限制（仅用于不拦截某些公开接口）。
     */
    int[] value() default {};
}
