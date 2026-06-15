package cn.edu.bjfu.nekocafe.mapper;

import cn.edu.bjfu.nekocafe.entity.TableStatus;
import cn.edu.bjfu.nekocafe.entity.TableStatusExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TableStatusMapper {
    long countByExample(TableStatusExample example);

    int deleteByExample(TableStatusExample example);

    int deleteByPrimaryKey(Integer tableId);

    int insert(TableStatus row);

    int insertSelective(TableStatus row);

    List<TableStatus> selectByExample(TableStatusExample example);

    TableStatus selectByPrimaryKey(Integer tableId);

    int updateByExampleSelective(@Param("row") TableStatus row, @Param("example") TableStatusExample example);

    int updateByExample(@Param("row") TableStatus row, @Param("example") TableStatusExample example);

    int updateByPrimaryKeySelective(TableStatus row);

    int updateByPrimaryKey(TableStatus row);

    /**
     * 乐观锁预约桌位：IDLE → RESERVED，version + 1
     * @return 受影响行数（0 表示并发冲突）
     */
    int reserveTableOptimistic(@Param("tableId") Integer tableId,
                               @Param("reservationId") Long reservationId,
                               @Param("expectedVersion") Integer expectedVersion);

    /**
     * 乐观锁释放桌位：RESERVED → IDLE，current_reservation_id → NULL，version + 1
     * @return 受影响行数（0 表示并发冲突）
     */
    int releaseTableOptimistic(@Param("tableId") Integer tableId,
                               @Param("reservationId") Long reservationId,
                               @Param("expectedVersion") Integer expectedVersion);

    /**
     * 乐观锁占用桌位：RESERVED → OCCUPIED，version + 1（下单确认后占用桌位）
     * @return 受影响行数（0 表示并发冲突）
     */
    int occupyTableOptimistic(@Param("tableId") Integer tableId,
                              @Param("expectedVersion") Integer expectedVersion);
}