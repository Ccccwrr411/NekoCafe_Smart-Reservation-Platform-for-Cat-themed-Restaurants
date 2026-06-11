package cn.edu.bjfu.nekocafe.service.impl;

import cn.edu.bjfu.nekocafe.common.ErrorCode;
import cn.edu.bjfu.nekocafe.entity.Payments;
import cn.edu.bjfu.nekocafe.entity.PaymentsExample;
import cn.edu.bjfu.nekocafe.entity.Reservations;
import cn.edu.bjfu.nekocafe.exception.BusinessException;
import cn.edu.bjfu.nekocafe.mapper.PaymentsMapper;
import cn.edu.bjfu.nekocafe.mapper.ReservationsMapper;
import cn.edu.bjfu.nekocafe.service.PayService;
import cn.edu.bjfu.nekocafe.vo.PayVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 支付服务实现
 *
 * 课设版使用模拟支付（不调真实微信 API）。
 * 支付状态流转：pending → paid（模拟付款后）
 *
 * ⚠️ 与 C 同学的 OrderServiceImpl 集成说明：
 *   未来 submitOrder / createReservation 创建订单后，应调用
 *   this.payService.createPayment(orderId, userId) 自动创建支付记录。
 *   目前由于 OrderServiceImpl 尚未实现，支付创建通过独立的
 *   POST /api/pay/create 接口手动触发。
 */
@Service
public class PayServiceImpl implements PayService {

    @Autowired
    private PaymentsMapper paymentsMapper;

    @Autowired
    private ReservationsMapper reservationsMapper;

    @Override
    @Transactional
    public PayVO createPayment(String orderId, Long userId) {
        // 1. 解析 orderId → reservationId
        Long reservationId = parseOrderId(orderId);

        // 2. 查预约记录
        Reservations reservation = reservationsMapper.selectByPrimaryKey(reservationId);
        if (reservation == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在：" + orderId);
        }
        if (!reservation.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作该订单");
        }

        // 3. 检查是否已有支付记录（幂等）
        PaymentsExample example = new PaymentsExample();
        example.createCriteria().andReservationIdEqualTo(reservationId);
        List<Payments> existing = paymentsMapper.selectByExample(example);
        if (!existing.isEmpty()) {
            Payments p = existing.get(0);
            if ("paid".equals(p.getStatus())) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "该订单已支付");
            }
            // 已有 pending 记录，直接返回
            return toPayVO(p, orderId);
        }

        // 4. 创建支付记录
        Payments payment = new Payments();
        payment.setReservationId(reservationId);
        payment.setPaymentMethod("wechat");
        payment.setAmount(reservation.getTotalAmount() != null
                ? reservation.getTotalAmount()
                : reservation.getOrderAmount());
        payment.setTransactionId("WX_MOCK_" + System.currentTimeMillis());
        payment.setStatus("pending");
        payment.setCreatedAt(new Date());
        paymentsMapper.insert(payment);

        return toPayVO(payment, orderId);
    }

    @Override
    @Transactional
    public Map<String, Object> simulatePay(String orderId) {
        Long reservationId = parseOrderId(orderId);

        // 1. 查支付记录
        PaymentsExample example = new PaymentsExample();
        example.createCriteria().andReservationIdEqualTo(reservationId);
        List<Payments> list = paymentsMapper.selectByExample(example);
        if (list.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "该订单未发起支付，请先调用 /api/pay/create");
        }

        Payments payment = list.get(0);

        // 2. 检查是否已支付
        if ("paid".equals(payment.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "该订单已支付，无需重复模拟");
        }

        // 3. 更新支付状态
        Date now = new Date();
        payment.setStatus("paid");
        payment.setPaidAt(now);
        paymentsMapper.updateByPrimaryKeySelective(payment);

        // 4. 更新预约状态
        Reservations reservation = reservationsMapper.selectByPrimaryKey(reservationId);
        if (reservation != null) {
            reservation.setStatus("confirmed");
            reservation.setUpdatedAt(now);
            reservationsMapper.updateByPrimaryKeySelective(reservation);
        }

        // 5. 返回结果
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("orderId", orderId);
        result.put("transactionId", payment.getTransactionId());
        result.put("amount", payment.getAmount());
        result.put("status", "paid");
        result.put("paidAt", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(now));
        result.put("message", "模拟支付成功（微信支付沙箱回调已模拟）");
        return result;
    }

    @Override
    public PayVO queryStatus(String orderId) {
        Long reservationId = parseOrderId(orderId);

        PaymentsExample example = new PaymentsExample();
        example.createCriteria().andReservationIdEqualTo(reservationId);
        List<Payments> list = paymentsMapper.selectByExample(example);
        if (list.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "该订单暂无支付记录");
        }

        return toPayVO(list.get(0), orderId);
    }

    // ==================== 私有工具方法 ====================

    /**
     * 解析 orderId（"ORD0000000001"）→ reservationId（Long）
     */
    private Long parseOrderId(String orderId) {
        if (orderId == null || !orderId.startsWith("ORD")) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "订单ID格式错误，应为 ORD + 10位数字");
        }
        try {
            return Long.parseLong(orderId.substring(3));
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "订单ID格式错误：" + orderId);
        }
    }

    /**
     * 支付实体 → PayVO
     */
    private PayVO toPayVO(Payments p, String orderId) {
        PayVO vo = new PayVO();
        vo.setPaymentId(p.getPaymentId());
        vo.setOrderId(orderId);
        vo.setAmount(p.getAmount());
        vo.setStatus(p.getStatus());
        vo.setTransactionId(p.getTransactionId());
        if (p.getPaidAt() != null) {
            vo.setPaidAt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(p.getPaidAt()));
        }
        return vo;
    }
}
