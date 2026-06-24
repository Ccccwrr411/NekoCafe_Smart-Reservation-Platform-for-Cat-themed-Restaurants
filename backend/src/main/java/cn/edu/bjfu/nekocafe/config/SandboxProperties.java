package cn.edu.bjfu.nekocafe.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 沙箱模式统一配置
 *
 * 沙箱模式（默认开启）：
 *   - 验证码：不调用短信 API，验证码直接返回给前端弹窗显示
 *   - 支付：返回 mock 支付参数，前端走模拟支付弹窗
 *
 * 正式模式（sandbox.enabled = false）：
 *   - 验证码：调用真实短信 API 发送（需配置短信服务商）
 *   - 支付：调用微信支付统一下单 API（需配置微信支付证书）
 */
@Component
@ConfigurationProperties(prefix = "nekocafe.sandbox")
public class SandboxProperties {

    /**
     * 是否启用沙箱模式（课设默认 true）
     */
    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
