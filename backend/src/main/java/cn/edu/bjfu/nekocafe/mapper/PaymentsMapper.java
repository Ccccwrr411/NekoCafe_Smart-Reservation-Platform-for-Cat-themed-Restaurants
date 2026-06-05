package cn.edu.bjfu.nekocafe.mapper;

import cn.edu.bjfu.nekocafe.entity.Payments;
import cn.edu.bjfu.nekocafe.entity.PaymentsExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface PaymentsMapper {
    long countByExample(PaymentsExample example);

    int deleteByExample(PaymentsExample example);

    int deleteByPrimaryKey(Long paymentId);

    int insert(Payments row);

    int insertSelective(Payments row);

    List<Payments> selectByExample(PaymentsExample example);

    Payments selectByPrimaryKey(Long paymentId);

    int updateByExampleSelective(@Param("row") Payments row, @Param("example") PaymentsExample example);

    int updateByExample(@Param("row") Payments row, @Param("example") PaymentsExample example);

    int updateByPrimaryKeySelective(Payments row);

    int updateByPrimaryKey(Payments row);
}