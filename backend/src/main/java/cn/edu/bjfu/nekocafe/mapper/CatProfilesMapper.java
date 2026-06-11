package cn.edu.bjfu.nekocafe.mapper;

import cn.edu.bjfu.nekocafe.entity.CatProfiles;
import cn.edu.bjfu.nekocafe.entity.CatProfilesExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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

    // ========== 推荐模块自定义 SQL ==========

    /**
     * R11: 按性格关键词筛选猫咪（如"温顺"）
     */
    List<CatProfiles> selectByPersonality(@Param("keyword") String keyword);
}
