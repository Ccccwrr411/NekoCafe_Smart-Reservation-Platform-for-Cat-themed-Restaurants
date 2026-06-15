package cn.edu.bjfu.nekocafe.mapper;

import cn.edu.bjfu.nekocafe.entity.RefundRecords;
import cn.edu.bjfu.nekocafe.entity.RefundRecordsExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface RefundRecordsMapper {
    long countByExample(RefundRecordsExample example);

    int deleteByExample(RefundRecordsExample example);

    int deleteByPrimaryKey(Long refundId);

    int insert(RefundRecords row);

    int insertSelective(RefundRecords row);

    List<RefundRecords> selectByExample(RefundRecordsExample example);

    RefundRecords selectByPrimaryKey(Long refundId);

    int updateByExampleSelective(@Param("row") RefundRecords row, @Param("example") RefundRecordsExample example);

    int updateByExample(@Param("row") RefundRecords row, @Param("example") RefundRecordsExample example);

    int updateByPrimaryKeySelective(RefundRecords row);

    int updateByPrimaryKey(RefundRecords row);
}