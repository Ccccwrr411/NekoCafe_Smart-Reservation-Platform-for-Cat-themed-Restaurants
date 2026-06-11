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

    // ========== 推荐模块自定义 SQL ==========

    /**
     * R10 + R13: 为推荐引擎查询可用桌位
     * 按桌位类型和容量过滤在售桌位，支持 VIP 包厢优先排序
     *
     * @param minCapacity 最小可容纳人数
     * @param tableType   桌位类型关键词（null 则不过滤），如 "vip"、"standard"
     * @param limit       最多返回数量
     */
    List<Tables> selectAvailableTablesForRecommend(
            @Param("minCapacity") int minCapacity,
            @Param("tableType") String tableType,
            @Param("limit") int limit);
}
