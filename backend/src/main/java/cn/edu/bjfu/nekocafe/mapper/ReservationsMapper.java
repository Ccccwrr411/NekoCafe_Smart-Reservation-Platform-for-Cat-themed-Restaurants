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

    /**
     * 按用户ID和状态列表查询预约记录。
     * 专用方法，对 status 字段使用 ::reservation_status 显式类型转换，
     * 解决 PostgreSQL 自定义枚举类型无法与 character varying 直接比较的问题。
     */
    List<Reservations> selectByUserIdAndStatuses(
            @Param("userId") Long userId,
            @Param("statuses") List<String> statuses);

    /**
     * 按门店ID和状态列表查询预约记录。
     * 专用方法，对 status 字段使用 ::reservation_status 显式类型转换。
     */
    List<Reservations> selectByStoreIdAndStatuses(
            @Param("storeId") Integer storeId,
            @Param("statuses") List<String> statuses);

    /**
     * 按门店ID查询全部预约记录（店员工作台用）。
     * 对 status 字段使用 ::reservation_status 显式类型转换。
     */
    List<Reservations> selectByStoreId(@Param("storeId") Integer storeId);
}