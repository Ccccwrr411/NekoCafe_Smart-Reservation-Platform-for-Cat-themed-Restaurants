package cn.edu.bjfu.nekocafe.mapper;

import cn.edu.bjfu.nekocafe.entity.Roles;
import cn.edu.bjfu.nekocafe.entity.RolesExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface RolesMapper {
    long countByExample(RolesExample example);

    int deleteByExample(RolesExample example);

    int deleteByPrimaryKey(Integer roleId);

    int insert(Roles row);

    int insertSelective(Roles row);

    List<Roles> selectByExample(RolesExample example);

    Roles selectByPrimaryKey(Integer roleId);

    int updateByExampleSelective(@Param("row") Roles row, @Param("example") RolesExample example);

    int updateByExample(@Param("row") Roles row, @Param("example") RolesExample example);

    int updateByPrimaryKeySelective(Roles row);

    int updateByPrimaryKey(Roles row);
}