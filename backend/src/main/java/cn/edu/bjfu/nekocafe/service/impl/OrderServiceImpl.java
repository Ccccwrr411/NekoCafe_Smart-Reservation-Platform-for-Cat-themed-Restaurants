package cn.edu.bjfu.nekocafe.service.impl;

import cn.edu.bjfu.nekocafe.dto.OrderSubmitDTO;
import cn.edu.bjfu.nekocafe.dto.ReservationCreateDTO;
import cn.edu.bjfu.nekocafe.dto.RescheduleDTO;
import cn.edu.bjfu.nekocafe.mapper.*;
import cn.edu.bjfu.nekocafe.service.OrderService;
import cn.edu.bjfu.nekocafe.vo.OrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

/**
 * 订单 & 预约服务实现
 * 负责人：___（建议2人协作：一人负责查询类，一人负责写入类）
 *
 * 实现要点：
 *   orderId 格式："ORD" + reservationId（补零到10位），如 ORD0000000001
 *   订单状态映射（reservations.status 字段）：
 *     pending → 待支付, confirmed → 已确认, completed → 已完成
 *     cancelled → 已取消, refunding → 退款中, refunded → 已退款
 *   durationMin 转 duration：durationMin / 60
 *   canCancel / canReschedule / canRefund 根据 status 和 reservationTime 判断
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private ReservationsMapper reservationsMapper;

    @Autowired
    private OrderItemsMapper orderItemsMapper;

    @Autowired
    private StoresMapper storesMapper;

    @Autowired
    private TablesMapper tablesMapper;

    @Autowired
    private RefundRecordsMapper refundRecordsMapper;

    @Autowired
    private PaymentsMapper paymentsMapper;

    @Override
    public List<OrderVO> listOrders(Long userId) {
        throw new UnsupportedOperationException("OrderServiceImpl.listOrders 尚未实现");
    }

    @Override
    public Map<String, Object> submitOrder(Long userId, OrderSubmitDTO dto) {
        throw new UnsupportedOperationException("OrderServiceImpl.submitOrder 尚未实现");
    }

    @Override
    public OrderVO getOrderDetail(String orderId) {
        throw new UnsupportedOperationException("OrderServiceImpl.getOrderDetail 尚未实现");
    }

    @Override
    public Map<String, Object> cancelOrder(Long userId, String orderId) {
        throw new UnsupportedOperationException("OrderServiceImpl.cancelOrder 尚未实现");
    }

    @Override
    public Map<String, Object> reschedule(Long userId, RescheduleDTO dto) {
        throw new UnsupportedOperationException("OrderServiceImpl.reschedule 尚未实现");
    }

    @Override
    public Map<String, Object> applyRefund(Long userId, String orderId) {
        throw new UnsupportedOperationException("OrderServiceImpl.applyRefund 尚未实现");
    }

    @Override
    public Map<String, Object> createReservation(Long userId, ReservationCreateDTO dto) {
        throw new UnsupportedOperationException("OrderServiceImpl.createReservation 尚未实现");
    }
}
