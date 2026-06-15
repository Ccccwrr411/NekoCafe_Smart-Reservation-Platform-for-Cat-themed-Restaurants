package cn.edu.bjfu.nekocafe.service.impl;

import cn.edu.bjfu.nekocafe.dto.QueueTakeDTO;
import cn.edu.bjfu.nekocafe.entity.Queue;
import cn.edu.bjfu.nekocafe.entity.QueueExample;
import cn.edu.bjfu.nekocafe.entity.Users;
import cn.edu.bjfu.nekocafe.mapper.QueueMapper;
import cn.edu.bjfu.nekocafe.mapper.UsersMapper;
import cn.edu.bjfu.nekocafe.service.QueueService;
import cn.edu.bjfu.nekocafe.vo.QueueStatusVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

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
    private UsersMapper usersMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /** CALLED 状态超时时间（分钟），超过自动变为 MISSED */
    private static final int CALLED_TIMEOUT_MINUTES = 5;

    /**
     * J-1 获取指定门店的排队状态
     *
     * 状态流转：WAITING → CALLED(叫号) → KNOWN(用户确认) / MISSED(5分钟未确认)
     *
     * 查询流程：
     *   1. 查该门店 status='WAITING' 的记录 → 等待队列
     *   2. 当前叫号从 Redis 读（callNumber 时写入）
     *   3. 检查当前用户的记录是否为 CALLED → 弹窗提示
     *   4. 检查所有 CALLED 超过5分钟的记录 → 自动改为 MISSED
     */
    @Override
    public QueueStatusVO getQueueStatus(Integer storeId, Long userId) {
        // --- 1: 自动将超时的 CALLED 转为 MISSED ---
        autoExpireCalled(storeId);

        // --- 2: 查等待队列（只显示 WAITING 状态的） ---
        QueueExample waitingExample = new QueueExample();
        waitingExample.createCriteria()
                .andStoreIdEqualTo(storeId)
                .andStatusEqualTo("WAITING");
        waitingExample.setOrderByClause("created_at ASC");
        List<Queue> waitingList = queueMapper.selectByExample(waitingExample);

        // --- 3: 当前叫号（从 Redis 读） ---
        int currentNumber = 0;
        String currentKey = "nekocafe:current:" + storeId;
        String currentVal = stringRedisTemplate.opsForValue().get(currentKey);
        if (currentVal != null && !currentVal.isEmpty()) {
            currentNumber = Integer.parseInt(currentVal);
        }

        // --- 4: 计算指标 ---
        int waitingCount = waitingList.size();
        int avgWaitMinutes = waitingCount * AVG_WAIT_PER_PERSON;

        // --- 5: 组装队列列表 + 找我的号码 ---
        List<QueueStatusVO.QueueItemVO> queueItemList = new ArrayList<>();
        Integer myNumber = null;
        Integer myWaitMinutes = 0;

        for (int i = 0; i < waitingList.size(); i++) {
            Queue q = waitingList.get(i);
            QueueStatusVO.QueueItemVO item = new QueueStatusVO.QueueItemVO();
            item.setQueueId(q.getQueueId());
            item.setNumber(parseQueueNumber(q.getQueueNumber()));
            item.setPersons(q.getPartySize());
            item.setType(q.getPreferredTableType());
            item.setAhead(i);
            item.setUserName(getUserNickname(q.getUserId()));

            boolean isMine = (userId != null && q.getUserId() != null && q.getUserId().equals(userId));
            item.setIsMine(isMine);
            queueItemList.add(item);

            if (isMine) {
                myNumber = parseQueueNumber(q.getQueueNumber());
                myWaitMinutes = i * AVG_WAIT_PER_PERSON;
            }
        }

        // --- 6: 检查当前用户是否被叫到（CALLED） ---
        boolean called = false;
        Long myCalledQueueId = null;
        if (userId != null && myNumber == null) {
            // 用户不在 WAITING 队列中，检查是否在 CALLED 状态
            QueueExample calledEx = new QueueExample();
            calledEx.createCriteria()
                    .andStoreIdEqualTo(storeId)
                    .andUserIdEqualTo(userId)
                    .andStatusEqualTo("CALLED");
            List<Queue> calledList = queueMapper.selectByExample(calledEx);
            if (!calledList.isEmpty()) {
                called = true;
                myCalledQueueId = calledList.get(0).getQueueId();
            }
        }

        // --- 6b: 查已叫号列表（CALLED + KNOWN，按时间倒序，最新叫的在最前） ---
        QueueExample calledOrKnownEx = new QueueExample();
        calledOrKnownEx.createCriteria()
                .andStoreIdEqualTo(storeId)
                .andStatusIn(Arrays.asList("CALLED", "KNOWN"));
        calledOrKnownEx.setOrderByClause("created_at DESC");
        List<Queue> calledOrKnownRaw = queueMapper.selectByExample(calledOrKnownEx);
        List<QueueStatusVO.QueueItemVO> calledListVO = new ArrayList<>();
        for (Queue q : calledOrKnownRaw) {
            QueueStatusVO.QueueItemVO item = new QueueStatusVO.QueueItemVO();
            item.setQueueId(q.getQueueId());
            item.setNumber(parseQueueNumber(q.getQueueNumber()));
            item.setPersons(q.getPartySize());
            item.setType(q.getPreferredTableType());
            item.setStatus(q.getStatus());  // CALLED 或 KNOWN
            item.setIsMine(userId != null && q.getUserId() != null && q.getUserId().equals(userId));
            item.setUserName(getUserNickname(q.getUserId()));
            calledListVO.add(item);
        }

        // --- 6c: 查过号列表（MISSED，按时间倒序） ---
        QueueExample missedEx = new QueueExample();
        missedEx.createCriteria()
                .andStoreIdEqualTo(storeId)
                .andStatusEqualTo("MISSED");
        missedEx.setOrderByClause("created_at DESC");
        List<Queue> missedRaw = queueMapper.selectByExample(missedEx);
        List<QueueStatusVO.QueueItemVO> missedListVO = new ArrayList<>();
        for (Queue q : missedRaw) {
            QueueStatusVO.QueueItemVO item = new QueueStatusVO.QueueItemVO();
            item.setQueueId(q.getQueueId());
            item.setNumber(parseQueueNumber(q.getQueueNumber()));
            item.setPersons(q.getPartySize());
            item.setType(q.getPreferredTableType());
            item.setStatus("MISSED");
            item.setIsMine(userId != null && q.getUserId() != null && q.getUserId().equals(userId));
            item.setUserName(getUserNickname(q.getUserId()));
            missedListVO.add(item);
        }

        // --- 7: 组装返回 VO ---
        QueueStatusVO vo = new QueueStatusVO();
        vo.setStoreId(storeId);
        vo.setWaitingCount(waitingCount);
        vo.setAvgWaitMinutes(avgWaitMinutes);
        vo.setCurrentNumber(currentNumber);
        vo.setMyNumber(myNumber);
        vo.setMyWaitMinutes(myWaitMinutes);
        vo.setMyQueueId(myCalledQueueId);
        vo.setCalled(called);
        vo.setQueueList(queueItemList);
        vo.setCalledList(calledListVO);
        vo.setMissedList(missedListVO);

        return vo;
    }

    /**
     * 将超过5分钟的 CALLED 记录自动转为 MISSED
     * 使用 calledAt（叫号时间）判断超时
     */
    private void autoExpireCalled(Integer storeId) {
        // 查所有 CALLED 记录
        QueueExample calledEx = new QueueExample();
        calledEx.createCriteria()
                .andStoreIdEqualTo(storeId)
                .andStatusEqualTo("CALLED");
        List<Queue> calledList = queueMapper.selectByExample(calledEx);

        Calendar expireThreshold = Calendar.getInstance();
        expireThreshold.add(Calendar.MINUTE, -CALLED_TIMEOUT_MINUTES);

        for (Queue q : calledList) {
            // 用 calledAt（叫号时间）判断是否超过5分钟
            Date calledAt = q.getCalledAt();
            if (calledAt != null && calledAt.before(expireThreshold.getTime())) {
                // 超过5分钟，改为 MISSED
                q.setStatus("MISSED");
                q.setCreatedAt(new Date());   // 每次操作更新 created_at
                queueMapper.updateByPrimaryKeySelective(q);
            }
        }
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

        // --- 3a: 删除旧排队记录（允许重复取号，覆盖旧的） ---
        QueueExample dupCheck = new QueueExample();
        dupCheck.createCriteria()
                .andStoreIdEqualTo(storeId)
                .andUserIdEqualTo(userId);
        List<Queue> oldRecords = queueMapper.selectByExample(dupCheck);
        for (Queue old : oldRecords) {
            queueMapper.deleteByPrimaryKey(old.getQueueId());
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

        Date now = new Date();
        Queue queue = new Queue();
        queue.setStoreId(storeId);
        queue.setUserId(userId);
        queue.setPartySize(dto.getPersons());
        queue.setPreferredTableType(dto.getType());
        queue.setStatus("WAITING");
        queue.setQueueNumber(queueNumber);
        queue.setCreatedAt(now);

        queueMapper.insertSelective(queue);

        // --- 3d: 查前方人数并计算预计等待 ---
        QueueExample countEx = new QueueExample();
        countEx.createCriteria()
                .andStoreIdEqualTo(storeId);
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
     * J-3 叫号 — 店员操作（先进先出）
     * 流程：
     *   1. 校验记录存在/归属门店/状态为 WAITING
     *   2. 将记录 status 更新为 CALLED
     *   3. 写入 Redis 当前叫号
     *   4. 客户端轮询检测到 CALLED 状态后弹窗，用户确认后改为 KNOWN
     */
    @Override
    @Transactional
    public Map<String, Object> callNumber(Integer storeId, Long queueId) {
        // 1. 查找并校验排队记录
        Queue record = queueMapper.selectByPrimaryKey(queueId);
        if (record == null) {
            throw new RuntimeException("排队记录不存在");
        }
        if (!record.getStoreId().equals(storeId)) {
            throw new RuntimeException("该排队记录不属于此门店");
        }
        if (!"WAITING".equals(record.getStatus())) {
            throw new RuntimeException("该号码已处理，无法重复叫号");
        }

        // 2. 记录信息备用
        Long userId = record.getUserId();
        int number = parseQueueNumber(record.getQueueNumber());
        int partySize = record.getPartySize() != null ? record.getPartySize() : 2;
        String tableType = record.getPreferredTableType();

        // 3. 更新为 CALLED（不删除！保留记录供用户查询和确认）
        record.setStatus("CALLED");
        record.setCalledAt(new Date());    // 记录叫号时间
        record.setCreatedAt(new Date());   // 每次操作更新 created_at
        queueMapper.updateByPrimaryKeySelective(record);

        // 4. 更新当前叫号到 Redis（门店维度，当天有效）
        String currentKey = "nekocafe:current:" + storeId;
        stringRedisTemplate.opsForValue().set(currentKey, String.valueOf(number),
                86400L, TimeUnit.SECONDS);

        // 5. 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("queueId", queueId);
        result.put("number", number);
        result.put("persons", partySize);
        result.put("type", tableType);

        return result;
    }

    /**
     * J-4 用户确认叫号 — 将 CALLED 改为 KNOWN
     */
    @Override
    @Transactional
    public void confirmNumber(Long userId, Long queueId) {
        Queue record = queueMapper.selectByPrimaryKey(queueId);
        if (record == null) {
            throw new RuntimeException("排队记录不存在");
        }
        if (!record.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作他人的排队记录");
        }
        if (!"CALLED".equals(record.getStatus())) {
            throw new RuntimeException("该号码当前无法确认");
        }

        record.setStatus("KNOWN");
        record.setCreatedAt(new Date());   // 每次操作更新 created_at
        queueMapper.updateByPrimaryKeySelective(record);
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

    /**
     * 根据用户ID获取昵称
     */
    private String getUserNickname(Long userId) {
        if (userId == null) return null;
        try {
            Users user = usersMapper.selectByPrimaryKey(userId);
            return user != null ? user.getNickname() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
