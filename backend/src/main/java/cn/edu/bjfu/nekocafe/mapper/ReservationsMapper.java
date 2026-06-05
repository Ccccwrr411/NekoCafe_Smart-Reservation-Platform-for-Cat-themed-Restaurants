package cn.edu.bjfu.nekocafe.mapper;

import cn.edu.bjfu.nekocafe.entity.Reservations;
import cn.edu.bjfu.nekocafe.entity.ReservationsExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ReservationsMapper {
    long countByExample(ReservationsExample example);

    int deleteByExample(ReservationsExample example);

    int deleteByPrimaryKey(Long reservationId);

    int insert(Reservations row);

    int insertSelective(Reservations row);

    List<Reservations> selectByExample(ReservationsExample example);

    Reservations selectByPrimaryKey(Long reservationId);

    int updateByExampleSelective(@Param("row") Reservations row, @Param("example") ReservationsExample example);

    int updateByExample(@Param("row") Reservations row, @Param("example") ReservationsExample example);

    int updateByPrimaryKeySelective(Reservations row);

    int updateByPrimaryKey(Reservations row);
}