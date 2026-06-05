package cn.edu.bjfu.nekocafe.mapper;

import cn.edu.bjfu.nekocafe.entity.PointsLog;
import cn.edu.bjfu.nekocafe.entity.PointsLogExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface PointsLogMapper {
    long countByExample(PointsLogExample example);

    int deleteByExample(PointsLogExample example);

    int deleteByPrimaryKey(Long logId);

    int insert(PointsLog row);

    int insertSelective(PointsLog row);

    List<PointsLog> selectByExample(PointsLogExample example);

    PointsLog selectByPrimaryKey(Long logId);

    int updateByExampleSelective(@Param("row") PointsLog row, @Param("example") PointsLogExample example);

    int updateByExample(@Param("row") PointsLog row, @Param("example") PointsLogExample example);

    int updateByPrimaryKeySelective(PointsLog row);

    int updateByPrimaryKey(PointsLog row);
}