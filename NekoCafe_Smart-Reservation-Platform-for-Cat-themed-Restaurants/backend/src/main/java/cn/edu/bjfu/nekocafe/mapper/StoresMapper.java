package cn.edu.bjfu.nekocafe.mapper;

import cn.edu.bjfu.nekocafe.entity.Stores;
import cn.edu.bjfu.nekocafe.entity.StoresExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface StoresMapper {
    long countByExample(StoresExample example);

    int deleteByExample(StoresExample example);

    int deleteByPrimaryKey(Integer storeId);

    int insert(Stores row);

    int insertSelective(Stores row);

    List<Stores> selectByExample(StoresExample example);

    Stores selectByPrimaryKey(Integer storeId);

    int updateByExampleSelective(@Param("row") Stores row, @Param("example") StoresExample example);

    int updateByExample(@Param("row") Stores row, @Param("example") StoresExample example);

    int updateByPrimaryKeySelective(Stores row);

    int updateByPrimaryKey(Stores row);
}