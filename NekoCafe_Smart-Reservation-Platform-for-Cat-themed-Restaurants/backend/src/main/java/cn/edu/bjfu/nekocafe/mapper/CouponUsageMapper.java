package cn.edu.bjfu.nekocafe.mapper;

import cn.edu.bjfu.nekocafe.entity.CouponUsage;
import cn.edu.bjfu.nekocafe.entity.CouponUsageExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface CouponUsageMapper {
    long countByExample(CouponUsageExample example);

    int deleteByExample(CouponUsageExample example);

    int deleteByPrimaryKey(Long usageId);

    int insert(CouponUsage row);

    int insertSelective(CouponUsage row);

    List<CouponUsage> selectByExample(CouponUsageExample example);

    CouponUsage selectByPrimaryKey(Long usageId);

    int updateByExampleSelective(@Param("row") CouponUsage row, @Param("example") CouponUsageExample example);

    int updateByExample(@Param("row") CouponUsage row, @Param("example") CouponUsageExample example);

    int updateByPrimaryKeySelective(CouponUsage row);

    int updateByPrimaryKey(CouponUsage row);
}