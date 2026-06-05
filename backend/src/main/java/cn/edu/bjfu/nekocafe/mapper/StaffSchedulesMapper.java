package cn.edu.bjfu.nekocafe.mapper;

import cn.edu.bjfu.nekocafe.entity.StaffSchedules;
import cn.edu.bjfu.nekocafe.entity.StaffSchedulesExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface StaffSchedulesMapper {
    long countByExample(StaffSchedulesExample example);

    int deleteByExample(StaffSchedulesExample example);

    int deleteByPrimaryKey(Long scheduleId);

    int insert(StaffSchedules row);

    int insertSelective(StaffSchedules row);

    List<StaffSchedules> selectByExample(StaffSchedulesExample example);

    StaffSchedules selectByPrimaryKey(Long scheduleId);

    int updateByExampleSelective(@Param("row") StaffSchedules row, @Param("example") StaffSchedulesExample example);

    int updateByExample(@Param("row") StaffSchedules row, @Param("example") StaffSchedulesExample example);

    int updateByPrimaryKeySelective(StaffSchedules row);

    int updateByPrimaryKey(StaffSchedules row);
}