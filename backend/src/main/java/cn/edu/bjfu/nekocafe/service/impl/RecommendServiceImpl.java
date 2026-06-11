package cn.edu.bjfu.nekocafe.service.impl;

import cn.edu.bjfu.nekocafe.mapper.CatProfilesMapper;
import cn.edu.bjfu.nekocafe.mapper.DishesMapper;
import cn.edu.bjfu.nekocafe.mapper.MemberExtMapper;
import cn.edu.bjfu.nekocafe.mapper.ReservationsMapper;
import cn.edu.bjfu.nekocafe.service.RecommendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;

/**
 * AI 推荐服务实现
 * 负责人：___（优先级最低，其他功能完成后再做）
 *
 * 课设简化实现方案（不需要真实 AI）：
 *   1. 读取 MemberExt.preferences 字段（JSON），解析用户喜好品种和口味
 *   2. 根据喜好匹配 cat_profiles 中品种相符的猫咪，计算 matchScore（0-100）
 *   3. 根据 order_items 历史统计用户最常点的菜品作为推荐
 *   4. 以上全用规则实现，不调外部 API
 */
@Service
public class RecommendServiceImpl implements RecommendService {

    @Autowired
    private MemberExtMapper memberExtMapper;

    @Autowired
    private CatProfilesMapper catProfilesMapper;

    @Autowired
    private DishesMapper dishesMapper;

    @Autowired
    private ReservationsMapper reservationsMapper;

    @Override
    public Map<String, Object> recommend(Long userId) {
        throw new UnsupportedOperationException("RecommendServiceImpl.recommend 尚未实现");
    }
}
