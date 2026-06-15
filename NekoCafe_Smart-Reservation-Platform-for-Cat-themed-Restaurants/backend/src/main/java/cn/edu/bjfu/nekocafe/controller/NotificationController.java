package cn.edu.bjfu.nekocafe.controller;

import cn.edu.bjfu.nekocafe.common.Result;
import cn.edu.bjfu.nekocafe.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 通知中心 Controller
 * M-1 GET /api/notifications/store?storeId=&page=&size=
 * M-2 GET /api/notifications/user?userId=&page=&size=
 * M-3 GET /api/notifications/unread/store?storeId=
 * M-4 GET /api/notifications/unread/user?userId=
 * M-5 POST /api/notifications/read
 * M-6 POST /api/notifications/read-all/store
 * M-7 POST /api/notifications/read-all/user
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /** M-1 门店通知列表（店员/店长） */
    @GetMapping("/store")
    public Result<List<Map<String, Object>>> getStoreNotifications(
            @RequestParam Integer storeId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "30") Integer size) {
        return Result.success(notificationService.getStoreNotifications(storeId, page, size));
    }

    /** M-2 用户通知列表（顾客） */
    @GetMapping("/user")
    public Result<List<Map<String, Object>>> getUserNotifications(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "30") Integer size) {
        return Result.success(notificationService.getUserNotifications(userId, page, size));
    }

    /** M-3 门店未读数量 */
    @GetMapping("/unread/store")
    public Result<Integer> getStoreUnreadCount(@RequestParam Integer storeId) {
        return Result.success(notificationService.getStoreUnreadCount(storeId));
    }

    /** M-4 用户未读数量 */
    @GetMapping("/unread/user")
    public Result<Integer> getUserUnreadCount(@RequestParam Long userId) {
        return Result.success(notificationService.getUserUnreadCount(userId));
    }

    /** M-5 标记单条已读 */
    @PostMapping("/read")
    public Result<Map<String, Object>> markAsRead(@RequestBody Map<String, Object> body) {
        Long notificationId = Long.valueOf(body.get("notificationId").toString());
        return Result.success(notificationService.markAsRead(notificationId));
    }

    /** M-6 门店全部已读 */
    @PostMapping("/read-all/store")
    public Result<Map<String, Object>> markAllStoreRead(@RequestBody Map<String, Object> body) {
        Integer storeId = Integer.valueOf(body.get("storeId").toString());
        return Result.success(notificationService.markAllStoreRead(storeId));
    }

    /** M-7 用户全部已读 */
    @PostMapping("/read-all/user")
    public Result<Map<String, Object>> markAllUserRead(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        return Result.success(notificationService.markAllUserRead(userId));
    }
}
