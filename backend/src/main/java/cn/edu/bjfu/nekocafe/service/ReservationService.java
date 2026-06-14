package cn.edu.bjfu.nekocafe.service;

import cn.edu.bjfu.nekocafe.dto.ReservationCreateDTO;
import cn.edu.bjfu.nekocafe.dto.RescheduleDTO;
import cn.edu.bjfu.nekocafe.vo.CurrentReservationVO;
import java.util.List;
import java.util.Map;

/**
 * 预约服务接口
 * 实现类：ReservationServiceImpl
 */
public interface ReservationService {

    /**
     * 创建预约（E-7，纯预约无点单）
     * 返回 orderId + status
     */
    Map<String, Object> createReservation(Long userId, ReservationCreateDTO dto);

    /**
     * 改约（E-5）
     */
    Map<String, Object> reschedule(Long userId, RescheduleDTO dto);

    /**
     * 获取用户当前 BOOKED 状态的预约列表（供点单页面选择）
     */
    List<CurrentReservationVO> getCurrentReservations(Long userId);

    /**
     * 重新激活已取消的订单（CANCEL_ORDER → BOOKED）
     * 取消订单时桌位保留不释放，点击"再来一单"直接复用同一预约
     */
    Map<String, Object> reactivateReservation(Long userId, String orderId);
}
