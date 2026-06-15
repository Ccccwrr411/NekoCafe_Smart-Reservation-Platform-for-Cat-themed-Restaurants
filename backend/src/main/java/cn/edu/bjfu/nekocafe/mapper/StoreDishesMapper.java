package cn.edu.bjfu.nekocafe.mapper;

import cn.edu.bjfu.nekocafe.entity.StoreDishes;
import cn.edu.bjfu.nekocafe.entity.StoreDishesExample;
import cn.edu.bjfu.nekocafe.entity.StoreDishesKey;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface StoreDishesMapper {
    long countByExample(StoreDishesExample example);

    int deleteByExample(StoreDishesExample example);

    int deleteByPrimaryKey(StoreDishesKey key);

    int insert(StoreDishes row);

    int insertSelective(StoreDishes row);

    List<StoreDishes> selectByExample(StoreDishesExample example);

    StoreDishes selectByPrimaryKey(StoreDishesKey key);

    int updateByExampleSelective(@Param("row") StoreDishes row, @Param("example") StoreDishesExample example);

    int updateByExample(@Param("row") StoreDishes row, @Param("example") StoreDishesExample example);

    int updateByPrimaryKeySelective(StoreDishes row);

    int updateByPrimaryKey(StoreDishes row);

    // ========== 推荐模块自定义 SQL ==========

    /**
     * 查询指定门店所有在售菜品 ID 列表
     * 用于推荐模块按门店过滤候选菜品
     */
    List<Integer> selectDishIdsByStore(@Param("storeId") Integer storeId);
}