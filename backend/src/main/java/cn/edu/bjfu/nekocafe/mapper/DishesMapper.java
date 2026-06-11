package cn.edu.bjfu.nekocafe.mapper;

import cn.edu.bjfu.nekocafe.entity.Dishes;
import cn.edu.bjfu.nekocafe.entity.DishesExample;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

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

    // ========== 推荐模块自定义 SQL ==========

    /**
     * R5 + R17: 全店热门菜品（按 category 分组取 Top-N）
     * 返回: 每个分类下销量最高的 N 道菜
     */
    List<Map<String, Object>> selectHotDishesByCategory(@Param("limitPerCategory") Integer limit);

    /**
     * R2: 按口味标签匹配菜品
     * 匹配 dishes.tags 字段中包含指定关键词的菜品
     */
    List<Dishes> selectByTagKeyword(@Param("keyword") String keyword);

    /**
     * R8/R9: 按 category + 时段/季节筛选菜品
     */
    List<Dishes> selectByCategoryAndActive(@Param("category") String category);

    /**
     * R19: 查询指定分类下按 dish_id 排序的第一道在售菜品（用于咖啡+甜品搭配推荐）
     * 使用标准分页查询替代非标准 setOrderByClause("... LIMIT 1") 用法
     */
    @Select("SELECT dish_id, name, category, price, image_url, description, tags, is_active " +
            "FROM public.dishes WHERE category = #{category} AND is_active = true " +
            "ORDER BY dish_id ASC LIMIT 1")
    Dishes selectFirstActiveDishByCategory(@Param("category") String category);
}
