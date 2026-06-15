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

    /**
     * 叫号（J-3）— 店员操作：先进先出
     * 流程：将排队记录 status 从 WAITING 改为 CALLED
     * 客户端轮询检测到 CALLED 状态后弹窗提示"叫到你的号了，请去及时预约"
     * @param storeId  门店ID
     * @param queueId  要叫号的队列记录ID（主键）
     * @return 叫号结果信息
     */
    Map<String, Object> callNumber(Integer storeId, Long queueId);

    /**
     * 确认叫号（J-4）— 用户操作：被叫号后点击确认
     * 流程：将排队记录 status 从 CALLED 改为 KNOWN
     * @param userId   当前用户ID
     * @param queueId  排队记录ID
     */
    void confirmNumber(Long userId, Long queueId);
}
