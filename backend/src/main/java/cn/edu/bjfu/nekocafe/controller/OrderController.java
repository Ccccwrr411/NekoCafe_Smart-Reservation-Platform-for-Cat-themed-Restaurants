package cn.edu.bjfu.nekocafe.controller;

import cn.edu.bjfu.nekocafe.common.Result;
import cn.edu.bjfu.nekocafe.dto.OrderSubmitDTO;
import cn.edu.bjfu.nekocafe.dto.ReservationCreateDTO;
import cn.edu.bjfu.nekocafe.dto.RescheduleDTO;
import cn.edu.bjfu.nekocafe.service.OrderService;
import cn.edu.bjfu.nekocafe.vo.CurrentReservationVO;
import cn.edu.bjfu.nekocafe.vo.OrderVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * 订单 & 预约 Controller
 * 负责人：___
 * 接口：E-1 ~ E-7，以及 /api/reservation/create
 *
 * userId 从 Token 中获取（由 AuthInterceptor 注入到 request.getAttribute("userId")）
 */
@RestController
@RequestMapping("/api")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /** E-1 订单列表（支持状态筛选 + 搜索） */
    @GetMapping("/orders")
    public Result<List<OrderVO>> listOrders(HttpServletRequest request,
                                            @RequestParam(required = false) Long userId,
                                            @RequestParam(required = false) String status,
                                            @RequestParam(required = false) String keyword) {
        Long uid = userId != null ? userId : (Long) request.getAttribute("userId");
        return Result.success(orderService.listOrders(uid, status, keyword));
    }

    /** E-2 提交订单（含点单） */
    @PostMapping("/order/submit")
    public Result<Map<String, Object>> submitOrder(@RequestBody OrderSubmitDTO dto,
                                                    HttpServletRequest request,
                                                    @RequestParam(required = false) Long userId) {
        Long uid = userId != null ? userId : (Long) request.getAttribute("userId");
        return Result.success(orderService.submitOrder(uid, dto));
    }

    /** E-3 订单详情 */
    @GetMapping("/order/detail")
    public Result<OrderVO> getOrderDetail(@RequestParam String orderId) {
        return Result.success(orderService.getOrderDetail(orderId));
    }

    /** E-4 取消订单 */
    @PostMapping("/order/cancel")
    public Result<Map<String, Object>> cancelOrder(@RequestBody Map<String, String> body,
                                                    HttpServletRequest request,
                                                    @RequestParam(required = false) Long userId) {
        Long uid = userId != null ? userId : (Long) request.getAttribute("userId");
        return Result.success(orderService.cancelOrder(uid, body.get("orderId")));
    }

    /** E-5 改约（已废弃，取消预约请使用 POST /api/order/cancel） */
    @PostMapping("/order/reschedule")
    public Result<Map<String, Object>> reschedule(@RequestBody RescheduleDTO dto,
                                                   HttpServletRequest request,
                                                   @RequestParam(required = false) Long userId) {
        Long uid = userId != null ? userId : (Long) request.getAttribute("userId");
        return Result.success(orderService.reschedule(uid, dto));
    }

    /** E-6 申请退款 */
    @PostMapping("/order/refund")
    public Result<Map<String, Object>> applyRefund(@RequestBody Map<String, String> body,
                                                    HttpServletRequest request,
                                                    @RequestParam(required = false) Long userId) {
        Long uid = userId != null ? userId : (Long) request.getAttribute("userId");
        return Result.success(orderService.applyRefund(uid, body.get("orderId")));
    }

    /** E-7 纯预约（无点单） */
    @PostMapping("/reservation/create")
    public Result<Map<String, Object>> createReservation(@RequestBody ReservationCreateDTO dto,
                                                          HttpServletRequest request,
                                                          @RequestParam(required = false) Long userId) {
        Long uid = userId != null ? userId : (Long) request.getAttribute("userId");
        return Result.success(orderService.createReservation(uid, dto));
    }

    /** 获取用户当前 BOOKED 状态的预约列表（供点单页面选择） */
    @GetMapping("/reservation/current")
    public Result<List<CurrentReservationVO>> getCurrentReservations(HttpServletRequest request,
                                                                      @RequestParam(required = false) Long userId) {
        Long uid = userId != null ? userId : (Long) request.getAttribute("userId");
        List<CurrentReservationVO> list = orderService.getCurrentReservations(uid);
        return Result.success(list);
    }

    /** 重新激活已取消的订单（CANCEL_ORDER → BOOKED），复用同一预约记录 */
    @PostMapping("/order/reactivate")
    public Result<Map<String, Object>> reactivateReservation(@RequestBody Map<String, String> body,
                                                              HttpServletRequest request,
                                                              @RequestParam(required = false) Long userId) {
        Long uid = userId != null ? userId : (Long) request.getAttribute("userId");
        return Result.success(orderService.reactivateReservation(uid, body.get("orderId")));
    }
}
