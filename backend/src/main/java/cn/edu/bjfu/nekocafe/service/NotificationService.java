package cn.edu.bjfu.nekocafe.service;

import java.util.List;
import java.util.Map;

public interface NotificationService {

    /** 获取门店通知列表（店员/店长用） */
    List<Map<String, Object>> getStoreNotifications(Integer storeId, Integer page, Integer size);

    /** 获取用户通知列表（顾客用） */
    List<Map<String, Object>> getUserNotifications(Long userId, Integer page, Integer size);

    /** 门店未读数（店员角标） */
    int getStoreUnreadCount(Integer storeId);

    /** 用户未读数（顾客角标） */
    int getUserUnreadCount(Long userId);

    /** 标记单条已读 */
    Map<String, Object> markAsRead(Long notificationId);

    /** 门店全部已读 */
    Map<String, Object> markAllStoreRead(Integer storeId);

    /** 用户全部已读 */
    Map<String, Object> markAllUserRead(Long userId);

    /** 创建通知（内部调用） */
    void createNotification(Integer storeId, Long userId, String targetRole,
                            String type, String title, String content,
                            String relatedType, Long relatedId);
}
