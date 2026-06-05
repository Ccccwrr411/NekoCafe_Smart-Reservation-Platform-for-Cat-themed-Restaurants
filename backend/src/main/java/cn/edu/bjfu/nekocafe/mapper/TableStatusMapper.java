package cn.edu.bjfu.nekocafe.mapper;

import cn.edu.bjfu.nekocafe.entity.TableStatus;
import cn.edu.bjfu.nekocafe.entity.TableStatusExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TableStatusMapper {
    long countByExample(TableStatusExample example);

    int deleteByExample(TableStatusExample example);

    int deleteByPrimaryKey(Integer tableId);

    int insert(TableStatus row);

    int insertSelective(TableStatus row);

    List<TableStatus> selectByExample(TableStatusExample example);

    TableStatus selectByPrimaryKey(Integer tableId);

    int updateByExampleSelective(@Param("row") TableStatus row, @Param("example") TableStatusExample example);

    int updateByExample(@Param("row") TableStatus row, @Param("example") TableStatusExample example);

    int updateByPrimaryKeySelective(TableStatus row);

    int updateByPrimaryKey(TableStatus row);
}