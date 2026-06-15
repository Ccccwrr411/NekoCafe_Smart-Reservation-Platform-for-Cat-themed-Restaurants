package cn.edu.bjfu.nekocafe.service.impl;

import cn.edu.bjfu.nekocafe.entity.Notifications;
import cn.edu.bjfu.nekocafe.mapper.NotificationsMapper;
import cn.edu.bjfu.nekocafe.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationsMapper notificationsMapper;

    private static final int PAGE_SIZE = 30;

    @Override
    public List<Map<String, Object>> getStoreNotifications(Integer storeId, Integer page, Integer size) {
        int limit = (size != null && size > 0) ? size : PAGE_SIZE;
        int offset = (page != null && page > 1) ? (page - 1) * limit : 0;

        List<Notifications> list = notificationsMapper.selectByStore(storeId, limit, offset);
        return formatList(list);
    }

    @Override
    public List<Map<String, Object>> getUserNotifications(Long userId, Integer page, Integer size) {
        int limit = (size != null && size > 0) ? size : PAGE_SIZE;
        int offset = (page != null && page > 1) ? (page - 1) * limit : 0;

        List<Notifications> list = notificationsMapper.selectByUser(userId, limit, offset);
        return formatList(list);
    }

    private List<Map<String, Object>> formatList(List<Notifications> list) {
        SimpleDateFormat dtFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        List<Map<String, Object>> result = new ArrayList<>();
        if (list != null) {
            for (Notifications n : list) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("notificationId", n.getNotificationId());
                row.put("storeId", n.getStoreId());
                row.put("type", n.getType());
                row.put("title", n.getTitle());
                row.put("content", n.getContent());
                row.put("relatedType", n.getRelatedType());
                row.put("relatedId", n.getRelatedId());
                row.put("isRead", n.getIsRead() != null && n.getIsRead());
                row.put("createdAt", n.getCreatedAt() != null ? dtFmt.format(n.getCreatedAt()) : null);
                result.add(row);
            }
        }
        return result;
    }

    @Override
    public int getStoreUnreadCount(Integer storeId) {
        return notificationsMapper.countUnreadByStore(storeId);
    }

    @Override
    public int getUserUnreadCount(Long userId) {
        return notificationsMapper.countUnreadByUser(userId);
    }

    @Override
    public Map<String, Object> markAsRead(Long notificationId) {
        Map<String, Object> result = new LinkedHashMap<>();
        int affected = notificationsMapper.markAsRead(notificationId);
        result.put("success", affected > 0);
        result.put("message", affected > 0 ? "已标记为已读" : "通知不存在");
        return result;
    }

    @Override
    public Map<String, Object> markAllStoreRead(Integer storeId) {
        Map<String, Object> result = new LinkedHashMap<>();
        int affected = notificationsMapper.markAllAsReadByStore(storeId);
        result.put("success", true);
        result.put("message", "已全部标记为已读");
        result.put("count", affected);
        return result;
    }

    @Override
    public Map<String, Object> markAllUserRead(Long userId) {
        Map<String, Object> result = new LinkedHashMap<>();
        int affected = notificationsMapper.markAllAsReadByUser(userId);
        result.put("success", true);
        result.put("message", "已全部标记为已读");
        result.put("count", affected);
        return result;
    }

    @Override
    public void createNotification(Integer storeId, Long userId, String targetRole,
                                   String type, String title, String content,
                                   String relatedType, Long relatedId) {
        Notifications n = new Notifications();
        n.setStoreId(storeId);
        n.setUserId(userId);
        n.setTargetRole(targetRole);
        n.setType(type);
        n.setTitle(title);
        n.setContent(content);
        n.setRelatedType(relatedType);
        n.setRelatedId(relatedId);
        n.setIsRead(false);
        notificationsMapper.insertSelective(n);
    }
}
