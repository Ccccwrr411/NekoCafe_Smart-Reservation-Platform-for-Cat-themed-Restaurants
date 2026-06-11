package cn.edu.bjfu.nekocafe.mapper;

import cn.edu.bjfu.nekocafe.entity.Tables;
import cn.edu.bjfu.nekocafe.entity.TablesExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TablesMapper {
    long countByExample(TablesExample example);

    int deleteByExample(TablesExample example);

    int deleteByPrimaryKey(Integer tableId);

    int insert(Tables row);

    int insertSelective(Tables row);

    List<Tables> selectByExample(TablesExample example);

    Tables selectByPrimaryKey(Integer tableId);

    int updateByExampleSelective(@Param("row") Tables row, @Param("example") TablesExample example);

    int updateByExample(@Param("row") Tables row, @Param("example") TablesExample example);

    int updateByPrimaryKeySelective(Tables row);

    int updateByPrimaryKey(Tables row);
}