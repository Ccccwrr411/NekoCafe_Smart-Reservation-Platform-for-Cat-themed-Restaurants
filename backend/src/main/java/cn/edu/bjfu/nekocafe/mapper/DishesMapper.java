package cn.edu.bjfu.nekocafe.mapper;

import cn.edu.bjfu.nekocafe.entity.Dishes;
import cn.edu.bjfu.nekocafe.entity.DishesExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface DishesMapper {
    long countByExample(DishesExample example);

    int deleteByExample(DishesExample example);

    int deleteByPrimaryKey(Integer dishId);

    int insert(Dishes row);

    int insertSelective(Dishes row);

    List<Dishes> selectByExample(DishesExample example);

    Dishes selectByPrimaryKey(Integer dishId);

    int updateByExampleSelective(@Param("row") Dishes row, @Param("example") DishesExample example);

    int updateByExample(@Param("row") Dishes row, @Param("example") DishesExample example);

    int updateByPrimaryKeySelective(Dishes row);

    int updateByPrimaryKey(Dishes row);
}