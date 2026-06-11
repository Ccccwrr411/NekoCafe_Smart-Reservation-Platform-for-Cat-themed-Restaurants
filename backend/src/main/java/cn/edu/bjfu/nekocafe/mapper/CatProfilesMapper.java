package cn.edu.bjfu.nekocafe.mapper;

import cn.edu.bjfu.nekocafe.entity.CatProfiles;
import cn.edu.bjfu.nekocafe.entity.CatProfilesExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface CatProfilesMapper {
    long countByExample(CatProfilesExample example);

    int deleteByExample(CatProfilesExample example);

    int deleteByPrimaryKey(Integer catId);

    int insert(CatProfiles row);

    int insertSelective(CatProfiles row);

    List<CatProfiles> selectByExample(CatProfilesExample example);

    CatProfiles selectByPrimaryKey(Integer catId);

    int updateByExampleSelective(@Param("row") CatProfiles row, @Param("example") CatProfilesExample example);

    int updateByExample(@Param("row") CatProfiles row, @Param("example") CatProfilesExample example);

    int updateByPrimaryKeySelective(CatProfiles row);

    int updateByPrimaryKey(CatProfiles row);
}