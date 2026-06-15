package cn.edu.bjfu.nekocafe.mapper;

import cn.edu.bjfu.nekocafe.entity.UserRoles;
import cn.edu.bjfu.nekocafe.entity.UserRolesExample;
import cn.edu.bjfu.nekocafe.entity.UserRolesKey;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UserRolesMapper {
    long countByExample(UserRolesExample example);

    int deleteByExample(UserRolesExample example);

    int deleteByPrimaryKey(UserRolesKey key);

    int insert(UserRoles row);

    int insertSelective(UserRoles row);

    List<UserRoles> selectByExample(UserRolesExample example);

    UserRoles selectByPrimaryKey(UserRolesKey key);

    int updateByExampleSelective(@Param("row") UserRoles row, @Param("example") UserRolesExample example);

    int updateByExample(@Param("row") UserRoles row, @Param("example") UserRolesExample example);

    int updateByPrimaryKeySelective(UserRoles row);

    int updateByPrimaryKey(UserRoles row);
}