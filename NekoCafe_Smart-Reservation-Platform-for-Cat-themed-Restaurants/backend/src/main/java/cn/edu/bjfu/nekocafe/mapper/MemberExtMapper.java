package cn.edu.bjfu.nekocafe.mapper;

import cn.edu.bjfu.nekocafe.entity.MemberExt;
import cn.edu.bjfu.nekocafe.entity.MemberExtExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface MemberExtMapper {
    long countByExample(MemberExtExample example);

    int deleteByExample(MemberExtExample example);

    int deleteByPrimaryKey(Long userId);

    int insert(MemberExt row);

    int insertSelective(MemberExt row);

    List<MemberExt> selectByExample(MemberExtExample example);

    MemberExt selectByPrimaryKey(Long userId);

    int updateByExampleSelective(@Param("row") MemberExt row, @Param("example") MemberExtExample example);

    int updateByExample(@Param("row") MemberExt row, @Param("example") MemberExtExample example);

    int updateByPrimaryKeySelective(MemberExt row);

    int updateByPrimaryKey(MemberExt row);
}