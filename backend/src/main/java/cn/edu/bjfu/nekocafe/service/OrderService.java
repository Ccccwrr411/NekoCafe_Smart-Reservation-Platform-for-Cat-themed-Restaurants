package cn.edu.bjfu.nekocafe.service;

import cn.edu.bjfu.nekocafe.dto.OrderSubmitDTO;
import cn.edu.bjfu.nekocafe.dto.ReservationCreateDTO;
import cn.edu.bjfu.nekocafe.dto.RescheduleDTO;
import cn.edu.bjfu.nekocafe.vo.OrderVO;
import java.util.List;
import java.util.Map;

/**
 * 订单 & 预约服务接口
 * 实现类：OrderServiceImpl
 */
public interface OrderService {

    /**
     * 获取用户订单列表（E-1）
     */
    List<OrderVO> listOrders(Long userId);

    /**
     * 提交点单订单（E-2）
     * 返回 orderId + totalAmount + finalAmount + payInfo
     */
    Map<String, Object> submitOrder(Long userId, OrderSubmitDTO dto);

    /**
     * 获取订单详情（E-3）
     */
    OrderVO getOrderDetail(String orderId);

    /**
     * 取消订单（E-4）
     * 返回 status + refundAmount
     */
    Map<String, Object> cancelOrder(Long userId, String orderId);

    /**
     * 改约（E-5）
     */
    Map<String, Object> reschedule(Long userId, RescheduleDTO dto);

    /**
     * 申请退款（E-6）
     * 返回 refundId + refundAmount + status
     */
    Map<String, Object> applyRefund(Long userId, String orderId);

    /**
     * 创建预约（E-7，纯预约无点单）
     * 返回 orderId + status
     */
    Map<String, Object> createReservation(Long userId, ReservationCreateDTO dto);
}
