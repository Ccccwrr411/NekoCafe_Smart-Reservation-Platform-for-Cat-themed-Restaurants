package cn.edu.bjfu.nekocafe.mapper;

import cn.edu.bjfu.nekocafe.entity.CatHealthRecords;
import cn.edu.bjfu.nekocafe.entity.CatHealthRecordsExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface CatHealthRecordsMapper {
    long countByExample(CatHealthRecordsExample example);

    int deleteByExample(CatHealthRecordsExample example);

    int deleteByPrimaryKey(Long recordId);

    int insert(CatHealthRecords row);

    int insertSelective(CatHealthRecords row);

    List<CatHealthRecords> selectByExample(CatHealthRecordsExample example);

    CatHealthRecords selectByPrimaryKey(Long recordId);

    int updateByExampleSelective(@Param("row") CatHealthRecords row, @Param("example") CatHealthRecordsExample example);

    int updateByExample(@Param("row") CatHealthRecords row, @Param("example") CatHealthRecordsExample example);

    int updateByPrimaryKeySelective(CatHealthRecords row);

    int updateByPrimaryKey(CatHealthRecords row);
}