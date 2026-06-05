package cn.edu.bjfu.nekocafe.service.impl;

import cn.edu.bjfu.nekocafe.dto.QueueTakeDTO;
import cn.edu.bjfu.nekocafe.mapper.QueueMapper;
import cn.edu.bjfu.nekocafe.service.QueueService;
import cn.edu.bjfu.nekocafe.vo.QueueStatusVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;

/**
 * 排队服务实现
 * 负责人：___
 *
 * 实现要点：
 *   getQueueStatus：查 queue 表 storeId=? AND status='waiting'，
 *     按 createTime 排序，计算位置和预计等待时间
 *   takeNumber：插入 queue 表一条记录，返回取到的号码
 *
 *   实时更新方案（课设建议）：
 *     前端轮询此接口（每10秒），不需要 WebSocket
 *     若实现 WebSocket 可以在 config 包新增 WebSocketConfig.java
 */
@Service
public class QueueServiceImpl implements QueueService {

    @Autowired
    private QueueMapper queueMapper;

    @Override
    public QueueStatusVO getQueueStatus(Integer storeId, Long userId) {
        throw new UnsupportedOperationException("QueueServiceImpl.getQueueStatus 尚未实现");
    }

    @Override
    public Map<String, Object> takeNumber(Long userId, QueueTakeDTO dto) {
        throw new UnsupportedOperationException("QueueServiceImpl.takeNumber 尚未实现");
    }
}
