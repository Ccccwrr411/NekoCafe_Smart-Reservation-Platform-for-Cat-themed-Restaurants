package cn.edu.bjfu.nekocafe.mapper;

import cn.edu.bjfu.nekocafe.entity.Promotions;
import cn.edu.bjfu.nekocafe.entity.PromotionsExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface PromotionsMapper {
    long countByExample(PromotionsExample example);

    int deleteByExample(PromotionsExample example);

    int deleteByPrimaryKey(Integer promoId);

    int insert(Promotions row);

    int insertSelective(Promotions row);

    List<Promotions> selectByExample(PromotionsExample example);

    Promotions selectByPrimaryKey(Integer promoId);

    int updateByExampleSelective(@Param("row") Promotions row, @Param("example") PromotionsExample example);

    int updateByExample(@Param("row") Promotions row, @Param("example") PromotionsExample example);

    int updateByPrimaryKeySelective(Promotions row);

    int updateByPrimaryKey(Promotions row);
}