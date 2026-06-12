package cn.edu.bjfu.nekocafe.service.impl;

import cn.edu.bjfu.nekocafe.dto.QueueTakeDTO;
import cn.edu.bjfu.nekocafe.entity.Queue;
import cn.edu.bjfu.nekocafe.entity.QueueExample;
import cn.edu.bjfu.nekocafe.mapper.QueueMapper;
import cn.edu.bjfu.nekocafe.service.QueueService;
import cn.edu.bjfu.nekocafe.vo.QueueStatusVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 排队服务实现
 * 负责人：E同学（lsf）
 *
 * 实现接口：
 *   J-1 getQueueStatus  — 查询门店排队状态（等待人数、当前叫号、我的号码等）
 *   J-2 takeNumber     — 用户取号入队（Redis INCR 原子发号）
 *
 *   实时更新方案（课设）：
 *     前端轮询此接口（每10秒），不需要 WebSocket
 */
@Service
public class QueueServiceImpl implements QueueService {

    /** 每人平均等待时间（分钟），课设简化为固定值 */
    private static final int AVG_WAIT_PER_PERSON = 5;

    @Autowired
    private QueueMapper queueMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * J-1 获取指定门店的排队状态
     *
     * 查询流程：
     *   1. 查该门店 status='waiting' 的记录 → 等待队列（按 created_at 升序）
     *   2. 查该门店 status='serving' 的记录 → 当前叫号（取最大号码值）
     *   3. 计算 waitingCount / avgWaitMinutes
     *   4. 遍历等待队列，组装 QueueItemVO 列表，同时找当前用户的 myNumber / myWaitMinutes
     *   5. 组装 QueueStatusVO 返回
     */
    @Override
    public QueueStatusVO getQueueStatus(Integer storeId, Long userId) {
        // --- 4a: 查等待队列 ---
        QueueExample waitingExample = new QueueExample();
        waitingExample.createCriteria()
                .andStoreIdEqualTo(storeId)
                .andStatusEqualTo("waiting");
        waitingExample.setOrderByClause("created_at ASC");
        List<Queue> waitingList = queueMapper.selectByExample(waitingExample);


        // --- 4b: 查当前叫号（status=serving 的最大号），无服务记录时默认 0 ---
        int currentNumber = 0;

//        // --- 4b: 查当前叫号（status=serving 的最大号） ---
//        Integer currentNumber = null;

        QueueExample servingExample = new QueueExample();
        servingExample.createCriteria()
                .andStoreIdEqualTo(storeId)
                .andStatusEqualTo("serving");
        List<Queue> servingList = queueMapper.selectByExample(servingExample);
        if (!servingList.isEmpty()) {

            for (Queue q : servingList) {
                int num = parseQueueNumber(q.getQueueNumber());
                if (num > currentNumber) {

//            int maxNum = 0;
//            for (Queue q : servingList) {
//                int num = parseQueueNumber(q.getQueueNumber());
//                if (num > maxNum) {
//                    maxNum = num;

                    currentNumber = num;
                }
            }
        }

        // --- 4c: 计算指标 ---
        // avgWaitMinutes = 新来一个人平均需要等多久 = 前方等待人数 × 每人5分钟
        int waitingCount = waitingList.size();
        int avgWaitMinutes = waitingCount * AVG_WAIT_PER_PERSON;

        // --- 4d: 查我的号 & 组装 queueList ---
        List<QueueStatusVO.QueueItemVO> queueItemList = new ArrayList<>();
        Integer myNumber = null;
        Integer myWaitMinutes = 0;

        for (int i = 0; i < waitingList.size(); i++) {
            Queue q = waitingList.get(i);
            QueueStatusVO.QueueItemVO item = new QueueStatusVO.QueueItemVO();
            item.setNumber(parseQueueNumber(q.getQueueNumber()));
            item.setPersons(q.getPartySize());
            item.setType(q.getPreferredTableType());
            item.setAhead(i);  // 前方有 i 个人在等（索引0表示排第1，前面0人）
            queueItemList.add(item);

            // 如果是当前用户，记录我的信息
            if (userId != null && q.getUserId() != null && q.getUserId().equals(userId)) {
                myNumber = parseQueueNumber(q.getQueueNumber());
                myWaitMinutes = i * AVG_WAIT_PER_PERSON;
            }
        }

        // --- 组装返回 VO ---
        QueueStatusVO vo = new QueueStatusVO();
        vo.setStoreId(storeId);
        vo.setWaitingCount(waitingCount);
        vo.setAvgWaitMinutes(avgWaitMinutes);
        vo.setCurrentNumber(currentNumber);
        vo.setMyNumber(myNumber);
        vo.setMyWaitMinutes(myWaitMinutes);
        vo.setQueueList(queueItemList);

        return vo;
    }

    /**
     * J-2 取号入队
     *
     * 流程：
     *   1. 校验是否已取号（同一用户同门店 status='waiting' 不能重复取号）
     *   2. 用 Redis INCR 原子生成当日排队序号（防并发重复）
     *   3. INSERT 一条 queue 记录，status='waiting'，queueNumber 格式为 "Q001"
     *   4. 返回取到的号码、前方人数、预计等待时间
     */
    @Override
    public Map<String, Object> takeNumber(Long userId, QueueTakeDTO dto) {
        Integer storeId = dto.getStoreId();

        // --- 3a: 校验重复取号 ---
        QueueExample dupCheck = new QueueExample();
        dupCheck.createCriteria()
                .andStoreIdEqualTo(storeId)
                .andUserIdEqualTo(userId)
                .andStatusEqualTo("waiting");
        long existCount = queueMapper.countByExample(dupCheck);
        if (existCount > 0) {
            throw new RuntimeException("您已在该门店排队中，请勿重复取号");
        }

        // --- 3b: Redis INCR 发号 ---
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String redisKey = "nekocafe:queue:" + storeId + ":" + today;
        Long seqNum = stringRedisTemplate.opsForValue().increment(redisKey);

        // 当天第一条记录时设置过期时间（第二天自动清理旧 key）
        if (seqNum != null && seqNum == 1) {
            stringRedisTemplate.expireAt(
                    redisKey,
                    new Date(System.currentTimeMillis() + 86400000L)  // 24小时后过期
            );
        }

        // --- 3c: INSERT 排队记录 ---
        String queueNumber = "Q" + String.format("%03d", seqNum);

        Queue queue = new Queue();
        queue.setStoreId(storeId);
        queue.setUserId(userId);
        queue.setPartySize(dto.getPersons());
        queue.setPreferredTableType(dto.getType());
        queue.setStatus("waiting");
        queue.setQueueNumber(queueNumber);
        queue.setCreatedAt(new Date());

        queueMapper.insertSelective(queue);

        // --- 3d: 查前方人数并计算预计等待 ---
        QueueExample countEx = new QueueExample();
        countEx.createCriteria()
                .andStoreIdEqualTo(storeId)
                .andStatusEqualTo("waiting");
        long totalWaiting = queueMapper.countByExample(countEx);
        int ahead = (int)(totalWaiting - 1);  // 前面有几人
        int estWaitMinutes = ahead * AVG_WAIT_PER_PERSON;

        // --- 构建返回结果 ---
        Map<String, Object> result = new HashMap<>();
        result.put("number", seqNum.intValue());       // 返回纯数字（与 API 契约一致）
        result.put("persons", dto.getPersons());
        result.put("type", dto.getType());
        result.put("ahead", ahead);
        result.put("estWaitMinutes", estWaitMinutes);

        return result;
    }

    /**
     * 解析排队号为纯数字
     * 数据库存的是 "Q001"/"Q012"，解析返回 1/12
     */
    private int parseQueueNumber(String queueNumber) {
        if (queueNumber == null || queueNumber.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(queueNumber.replaceFirst("^Q", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
