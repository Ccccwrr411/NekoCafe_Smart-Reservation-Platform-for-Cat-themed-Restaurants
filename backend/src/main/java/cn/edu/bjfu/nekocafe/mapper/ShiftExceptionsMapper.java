package cn.edu.bjfu.nekocafe.mapper;

import cn.edu.bjfu.nekocafe.entity.ShiftExceptions;
import cn.edu.bjfu.nekocafe.entity.ShiftExceptionsExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ShiftExceptionsMapper {
    long countByExample(ShiftExceptionsExample example);

    int deleteByExample(ShiftExceptionsExample example);

    int deleteByPrimaryKey(Long exceptionId);

    int insert(ShiftExceptions row);

    int insertSelective(ShiftExceptions row);

    List<ShiftExceptions> selectByExample(ShiftExceptionsExample example);

    ShiftExceptions selectByPrimaryKey(Long exceptionId);

    int updateByExampleSelective(@Param("row") ShiftExceptions row, @Param("example") ShiftExceptionsExample example);

    int updateByExample(@Param("row") ShiftExceptions row, @Param("example") ShiftExceptionsExample example);

    int updateByPrimaryKeySelective(ShiftExceptions row);

    int updateByPrimaryKey(ShiftExceptions row);
}