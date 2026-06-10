package cn.edu.bjfu.nekocafe.mapper;

import cn.edu.bjfu.nekocafe.entity.OrderItems;
import cn.edu.bjfu.nekocafe.entity.OrderItemsExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface OrderItemsMapper {
    long countByExample(OrderItemsExample example);

    int deleteByExample(OrderItemsExample example);

    int deleteByPrimaryKey(Long itemId);

    int insert(OrderItems row);

    int insertSelective(OrderItems row);

    List<OrderItems> selectByExample(OrderItemsExample example);

    OrderItems selectByPrimaryKey(Long itemId);

    int updateByExampleSelective(@Param("row") OrderItems row, @Param("example") OrderItemsExample example);

    int updateByExample(@Param("row") OrderItems row, @Param("example") OrderItemsExample example);

    int updateByPrimaryKeySelective(OrderItems row);

    int updateByPrimaryKey(OrderItems row);

    // ========== 推荐模块自定义 SQL ==========

    /**
     * R4: 统计用户历史点餐频次
     * 返回: [{dishId, totalQuantity}]
     */
    List<Map<String, Object>> selectDishFrequencyByUserId(@Param("userId") Long userId);

    /**
     * R16 + R17: 全店销量排行（协同过滤兜底）
     * 返回: [{dishId, orderCount}]
     */
    List<Map<String, Object>> selectGlobalDishRanking(@Param("limit") Integer limit);

    /**
     * R16: 同偏好用户的菜品偏好（模拟协同过滤）
     * 查找与当前用户口味相同的其他用户最爱点的菜
     * 返回: [{dishId, orderCount}]
     */
    List<Map<String, Object>> selectCollaborativeFilterDishes(
            @Param("currentUserId") Long currentUserId,
            @Param("flavorPreference") String flavorPreference,
            @Param("limit") Integer limit);
}
