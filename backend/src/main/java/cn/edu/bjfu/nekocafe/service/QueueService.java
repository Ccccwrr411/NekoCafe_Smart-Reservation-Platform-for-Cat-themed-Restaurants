package cn.edu.bjfu.nekocafe.service;

import cn.edu.bjfu.nekocafe.dto.QueueTakeDTO;
import cn.edu.bjfu.nekocafe.vo.QueueStatusVO;
import java.util.Map;

/**
 * 排队服务接口
 * 实现类：QueueServiceImpl
 */
public interface QueueService {

    /**
     * 获取指定门店的排队状态（J-1）
     * 如该用户已取号，则 myNumber 有值
     */
    QueueStatusVO getQueueStatus(Integer storeId, Long userId);

    /**
     * 取号（J-2）
     * 返回 number + persons + type + ahead + estWaitMinutes
     */
    Map<String, Object> takeNumber(Long userId, QueueTakeDTO dto);
}
