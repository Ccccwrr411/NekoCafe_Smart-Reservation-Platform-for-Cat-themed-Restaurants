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

    /**
     * 总部运营：按门店统计今日实收金额。
     * JOIN reservations 获取 store_id，筛选 payments.status='PAID' 且 paid_at 为今日。
     */
    Long sumTodayPaidByStoreId(@Param("storeId") Integer storeId);
}