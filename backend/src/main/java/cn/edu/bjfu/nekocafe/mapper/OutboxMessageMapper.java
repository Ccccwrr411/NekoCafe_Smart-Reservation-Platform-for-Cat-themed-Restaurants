package cn.edu.bjfu.nekocafe.mapper;

import cn.edu.bjfu.nekocafe.entity.OutboxMessage;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 本地消息表 Mapper（注解式，无需 XML）。
 */
@Mapper
public interface OutboxMessageMapper {

    @Insert("INSERT INTO outbox_message (exchange, routing_key, payload, msg_type, status, retry_count, created_at) "
            + "VALUES (#{exchange}, #{routingKey}, #{payload}, #{msgType}, 'PENDING', 0, now())")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(OutboxMessage msg);

    /** 取一批待投递消息（按 id 升序），FOR UPDATE SKIP LOCKED 支持多实例并行投递不冲突。 */
    @Select("SELECT id, exchange, routing_key AS routingKey, payload, msg_type AS msgType, "
            + "status, retry_count AS retryCount, created_at AS createdAt, sent_at AS sentAt "
            + "FROM outbox_message WHERE status = 'PENDING' "
            + "ORDER BY id ASC LIMIT #{limit} FOR UPDATE SKIP LOCKED")
    List<OutboxMessage> lockPendingBatch(@Param("limit") int limit);

    @Update("UPDATE outbox_message SET status = 'SENT', sent_at = now() WHERE id = #{id}")
    int markSent(@Param("id") Long id);

    @Update("UPDATE outbox_message SET retry_count = retry_count + 1 WHERE id = #{id}")
    int incrementRetry(@Param("id") Long id);
}
