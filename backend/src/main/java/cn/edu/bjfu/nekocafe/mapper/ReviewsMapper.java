package cn.edu.bjfu.nekocafe.mapper;

import cn.edu.bjfu.nekocafe.entity.Reviews;
import cn.edu.bjfu.nekocafe.entity.ReviewsExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ReviewsMapper {
    long countByExample(ReviewsExample example);

    int deleteByExample(ReviewsExample example);

    int deleteByPrimaryKey(Long reviewId);

    int insert(Reviews row);

    int insertSelective(Reviews row);

    List<Reviews> selectByExample(ReviewsExample example);

    Reviews selectByPrimaryKey(Long reviewId);

    int updateByExampleSelective(@Param("row") Reviews row, @Param("example") ReviewsExample example);

    int updateByExample(@Param("row") Reviews row, @Param("example") ReviewsExample example);

    int updateByPrimaryKeySelective(Reviews row);

    int updateByPrimaryKey(Reviews row);
}