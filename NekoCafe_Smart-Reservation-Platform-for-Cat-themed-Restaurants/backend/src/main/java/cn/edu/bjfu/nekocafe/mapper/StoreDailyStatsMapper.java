package cn.edu.bjfu.nekocafe.mapper;

import cn.edu.bjfu.nekocafe.entity.StoreDailyStats;
import cn.edu.bjfu.nekocafe.entity.StoreDailyStatsExample;
import cn.edu.bjfu.nekocafe.entity.StoreDailyStatsKey;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface StoreDailyStatsMapper {
    long countByExample(StoreDailyStatsExample example);

    int deleteByExample(StoreDailyStatsExample example);

    int deleteByPrimaryKey(StoreDailyStatsKey key);

    int insert(StoreDailyStats row);

    int insertSelective(StoreDailyStats row);

    List<StoreDailyStats> selectByExample(StoreDailyStatsExample example);

    StoreDailyStats selectByPrimaryKey(StoreDailyStatsKey key);

    int updateByExampleSelective(@Param("row") StoreDailyStats row, @Param("example") StoreDailyStatsExample example);

    int updateByExample(@Param("row") StoreDailyStats row, @Param("example") StoreDailyStatsExample example);

    int updateByPrimaryKeySelective(StoreDailyStats row);

    int updateByPrimaryKey(StoreDailyStats row);
}