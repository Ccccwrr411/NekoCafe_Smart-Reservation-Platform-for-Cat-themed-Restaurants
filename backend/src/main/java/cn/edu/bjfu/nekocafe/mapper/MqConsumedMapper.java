package cn.edu.bjfu.nekocafe.mapper;

import org.apache.ibatis.annotations.*;

/**
 * 消费幂等 / 结算协调表 Mapper。
 *
 * 核心是 INSERT ... ON CONFLICT DO NOTHING 的原子抢占：
 *   - 消费者用 applied=TRUE 抢占；抢到才真正落库副作用
 *   - 取消订单用 applied=FALSE 抢占（墓碑），抢到则说明副作用尚未应用，无需回滚
 */
@Mapper
public interface MqConsumedMapper {

    /**
     * 尝试抢占。返回 1 表示本次抢到（之前不存在），0 表示已被占用。
     */
    @Insert("INSERT INTO mq_consumed (message_id, applied, created_at) "
            + "VALUES (#{messageId}, #{applied}, now()) "
            + "ON CONFLICT (message_id) DO NOTHING")
    int tryClaim(@Param("messageId") String messageId, @Param("applied") boolean applied);

    /** 读取已存在记录的 applied 标记（抢占失败后判断到底是「已应用」还是「墓碑」）。 */
    @Select("SELECT applied FROM mq_consumed WHERE message_id = #{messageId}")
    Boolean selectApplied(@Param("messageId") String messageId);
}
