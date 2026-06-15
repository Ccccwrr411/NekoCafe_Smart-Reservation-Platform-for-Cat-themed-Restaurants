package cn.edu.bjfu.nekocafe.mapper;

import cn.edu.bjfu.nekocafe.entity.Queue;
import cn.edu.bjfu.nekocafe.entity.QueueExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface QueueMapper {
    long countByExample(QueueExample example);

    int deleteByExample(QueueExample example);

    int deleteByPrimaryKey(Long queueId);

    int insert(Queue row);

    int insertSelective(Queue row);

    List<Queue> selectByExample(QueueExample example);

    Queue selectByPrimaryKey(Long queueId);

    int updateByExampleSelective(@Param("row") Queue row, @Param("example") QueueExample example);

    int updateByExample(@Param("row") Queue row, @Param("example") QueueExample example);

    int updateByPrimaryKeySelective(Queue row);

    int updateByPrimaryKey(Queue row);
}