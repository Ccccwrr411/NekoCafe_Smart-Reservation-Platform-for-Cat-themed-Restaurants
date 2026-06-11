package cn.edu.bjfu.nekocafe.service;

import java.util.Map;

/**
 * AI 推荐服务接口
 * 实现类：RecommendServiceImpl
 */
public interface RecommendService {

    /**
     * 个性化推荐（H-1）
     * 根据用户历史偏好推荐桌位和菜品
     * 返回 reason + tables + dishes + userProfile
     *
     * 课设简化方案：可基于 MemberExt.preferences 字段实现规则推荐，
     * 无需真实 AI 模型
     */
    Map<String, Object> recommend(Long userId);
}
