package cn.edu.bjfu.nekocafe.mapper;

import cn.edu.bjfu.nekocafe.entity.Users;
import cn.edu.bjfu.nekocafe.entity.UsersExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UsersMapper {
    long countByExample(UsersExample example);

    int deleteByExample(UsersExample example);

    int deleteByPrimaryKey(Long userId);

    int insert(Users row);

    int insertSelective(Users row);

    List<Users> selectByExample(UsersExample example);

    Users selectByPrimaryKey(Long userId);

    int updateByExampleSelective(@Param("row") Users row, @Param("example") UsersExample example);

    int updateByExample(@Param("row") Users row, @Param("example") UsersExample example);

    int updateByPrimaryKeySelective(Users row);

    int updateByPrimaryKey(Users row);
}