package cn.edu.bjfu.nekocafe.controller;

import cn.edu.bjfu.nekocafe.common.Result;
import cn.edu.bjfu.nekocafe.service.PayService;
import cn.edu.bjfu.nekocafe.vo.PayVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 支付 Controller（微信支付沙箱模拟）
 *
 * 课设版不调真实微信 API，使用模拟支付流程。
 * 接口：
 *   POST /api/pay/create?orderId=xxx   — 发起支付
 *   POST /api/pay/simulate?orderId=xxx — 模拟支付完成
 *   GET  /api/pay/status?orderId=xxx   — 查询支付状态
 */
@RestController
@RequestMapping("/api/pay")
public class PayController {

    @Autowired
    private PayService payService;

    /**
     * 发起支付（模拟微信统一下单）
     * 创建一个 payments 表记录，返回支付信息
     */
    @PostMapping("/create")
    public Result<PayVO> create(@RequestParam String orderId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(payService.createPayment(orderId, userId));
    }

    /**
     * 模拟支付完成（替代微信支付异步回调）
     * 更新支付状态为 paid，预约状态为 confirmed
     */
    @PostMapping("/simulate")
    public Result<Map<String, Object>> simulate(@RequestParam String orderId) {
        return Result.success(payService.simulatePay(orderId));
    }

    /**
     * 查询支付状态
     */
    @GetMapping("/status")
    public Result<PayVO> status(@RequestParam String orderId) {
        return Result.success(payService.queryStatus(orderId));
    }
}
