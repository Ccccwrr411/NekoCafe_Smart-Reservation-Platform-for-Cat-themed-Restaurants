package cn.edu.bjfu.nekocafe.mapper;

import cn.edu.bjfu.nekocafe.entity.StaffShifts;
import cn.edu.bjfu.nekocafe.entity.StaffShiftsExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface StaffShiftsMapper {
    long countByExample(StaffShiftsExample example);

    int deleteByExample(StaffShiftsExample example);

    int deleteByPrimaryKey(Integer shiftId);

    int insert(StaffShifts row);

    int insertSelective(StaffShifts row);

    List<StaffShifts> selectByExample(StaffShiftsExample example);

    StaffShifts selectByPrimaryKey(Integer shiftId);

    int updateByExampleSelective(@Param("row") StaffShifts row, @Param("example") StaffShiftsExample example);

    int updateByExample(@Param("row") StaffShifts row, @Param("example") StaffShiftsExample example);

    int updateByPrimaryKeySelective(StaffShifts row);

    int updateByPrimaryKey(StaffShifts row);
}