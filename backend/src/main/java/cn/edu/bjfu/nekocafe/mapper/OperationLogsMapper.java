package cn.edu.bjfu.nekocafe.mapper;

import cn.edu.bjfu.nekocafe.entity.OperationLogs;
import cn.edu.bjfu.nekocafe.entity.OperationLogsExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface OperationLogsMapper {
    long countByExample(OperationLogsExample example);

    int deleteByExample(OperationLogsExample example);

    int deleteByPrimaryKey(Long logId);

    int insert(OperationLogs row);

    int insertSelective(OperationLogs row);

    List<OperationLogs> selectByExample(OperationLogsExample example);

    OperationLogs selectByPrimaryKey(Long logId);

    int updateByExampleSelective(@Param("row") OperationLogs row, @Param("example") OperationLogsExample example);

    int updateByExample(@Param("row") OperationLogs row, @Param("example") OperationLogsExample example);

    int updateByPrimaryKeySelective(OperationLogs row);

    int updateByPrimaryKey(OperationLogs row);
}