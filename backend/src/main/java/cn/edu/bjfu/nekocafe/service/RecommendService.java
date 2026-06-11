package cn.edu.bjfu.nekocafe.service;

import java.util.Map;

/**
 * AI 推荐服务接口
 * 实现类：RecommendServiceImpl
 *
 * 多策略融合规则引擎：
 *   维度A: 冷启动/降级 (R1/R5/R6/R17)
 *   维度B: 个性化匹配 (R2/R3/R4/R16)
 *   维度C: 上下文感知 (R7/R8/R9/R10)
 *   维度D: 会员分层 (R11/R13/R14/R15)
 *   维度E: 业务约束 (R18/R19)
 */
public interface RecommendService {

    /**
     * 个性化推荐（H-1）
     *
     * @param userId           用户ID（从 JWT 解析）
     * @param companionCount   同行人数，用于 R10 桌位匹配（默认=1）
     * @param hasChild         是否带小孩，用于 R11 温顺猫筛选（默认=false）
     * @return 推荐结果 Map{reason, cats, dishes, tables, userProfile}
     */
    Map<String, Object> recommend(Long userId, Integer companionCount, Boolean hasChild);
}
