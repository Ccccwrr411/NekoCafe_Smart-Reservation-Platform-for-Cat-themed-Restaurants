package cn.edu.bjfu.nekocafe.mapper;

import cn.edu.bjfu.nekocafe.entity.UserCoupons;
import cn.edu.bjfu.nekocafe.entity.UserCouponsExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UserCouponsMapper {
    long countByExample(UserCouponsExample example);

    int deleteByExample(UserCouponsExample example);

    int deleteByPrimaryKey(Long couponId);

    int insert(UserCoupons row);

    int insertSelective(UserCoupons row);

    List<UserCoupons> selectByExample(UserCouponsExample example);

    UserCoupons selectByPrimaryKey(Long couponId);

    int updateByExampleSelective(@Param("row") UserCoupons row, @Param("example") UserCouponsExample example);

    int updateByExample(@Param("row") UserCoupons row, @Param("example") UserCouponsExample example);

    int updateByPrimaryKeySelective(UserCoupons row);

    int updateByPrimaryKey(UserCoupons row);
}