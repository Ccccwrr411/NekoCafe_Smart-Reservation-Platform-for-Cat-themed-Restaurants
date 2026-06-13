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
     * @param userId  用户ID
     * @param status  状态筛选（前端值：booked/confirmed/completed/cancelled/refunding/refunded，null=全部）
     * @param keyword 搜索关键词（匹配门店名/桌位名/订单号）
     */
    List<OrderVO> listOrders(Long userId, String status, String keyword);

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
     * 申请退款（E-6）
     * 返回 refundId + refundAmount + status
     */
    Map<String, Object> applyRefund(Long userId, String orderId);
}
