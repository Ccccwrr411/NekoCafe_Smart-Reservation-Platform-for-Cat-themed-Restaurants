package cn.edu.bjfu.nekocafe.mapper;

import cn.edu.bjfu.nekocafe.entity.OrderItems;
import cn.edu.bjfu.nekocafe.entity.OrderItemsExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

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
}