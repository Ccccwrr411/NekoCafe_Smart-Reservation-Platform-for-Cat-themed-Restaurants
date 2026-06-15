package cn.edu.bjfu.nekocafe.mapper;

import cn.edu.bjfu.nekocafe.entity.Notifications;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface NotificationsMapper {

    int insertSelective(Notifications record);

    Notifications selectByPrimaryKey(Long notificationId);

    List<Notifications> selectByStore(@Param("storeId") Integer storeId,
                                       @Param("limit") Integer limit,
                                       @Param("offset") Integer offset);

    List<Notifications> selectByUser(@Param("userId") Long userId,
                                      @Param("limit") Integer limit,
                                      @Param("offset") Integer offset);

    int countUnreadByStore(@Param("storeId") Integer storeId);

    int countUnreadByUser(@Param("userId") Long userId);

    int markAsRead(@Param("notificationId") Long notificationId);

    int markAllAsReadByStore(@Param("storeId") Integer storeId);

    int markAllAsReadByUser(@Param("userId") Long userId);
}
