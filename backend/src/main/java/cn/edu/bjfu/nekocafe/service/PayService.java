package cn.edu.bjfu.nekocafe.service;

import cn.edu.bjfu.nekocafe.vo.PayVO;

import java.util.Map;

/**
 * 支付服务接口
 * 课设版使用模拟支付（不调真实微信 API）
 */
public interface PayService {

    /**
     * 发起支付（模拟微信统一下单）
     * 创建一条 payments 记录，返回支付信息
     *
     * @param orderId 订单ID，格式 "ORD" + reservationId 补零到10位
     * @param userId  用户ID
     * @return PayVO 支付信息
     */
    PayVO createPayment(String orderId, Long userId);

    /**
     * 模拟支付完成（替代微信支付回调）
     * 将支付状态改为 paid，预约状态改为 confirmed
     *
     * @param orderId 订单ID
     * @return 支付结果
     */
    Map<String, Object> simulatePay(String orderId);

    /**
     * 查询支付状态
     *
     * @param orderId 订单ID
     * @return PayVO 支付信息
     */
    PayVO queryStatus(String orderId);
}
