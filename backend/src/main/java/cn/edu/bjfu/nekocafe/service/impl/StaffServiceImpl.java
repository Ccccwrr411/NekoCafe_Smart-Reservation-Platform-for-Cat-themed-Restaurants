package cn.edu.bjfu.nekocafe.service.impl;

import cn.edu.bjfu.nekocafe.mapper.StoreDailyStatsMapper;
import cn.edu.bjfu.nekocafe.mapper.TableStatusMapper;
import cn.edu.bjfu.nekocafe.service.StaffService;
import cn.edu.bjfu.nekocafe.vo.DashboardMetricsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

/**
 * 店员 & 数据看板服务实现
 * 负责人：___
 *
 * 实现要点：
 *   getDashboardMetrics：
 *     坪效 / 翻台率 / 复购率 → 查 store_daily_stats 表按日期聚合
 *     todayOverview → 查今日 reservations 表统计
 *   getStaffTables：查 table_status 表，JOIN reservations 获取当前占用信息
 *   getAlerts：查 shift_exceptions 表（overstay / no_show / equipment 等类型）
 */
@Service
public class StaffServiceImpl implements StaffService {

    @Autowired
    private StoreDailyStatsMapper storeDailyStatsMapper;

    @Autowired
    private TableStatusMapper tableStatusMapper;

    @Override
    public DashboardMetricsVO getDashboardMetrics(Integer storeId, String range) {
        throw new UnsupportedOperationException("StaffServiceImpl.getDashboardMetrics 尚未实现");
    }

    @Override
    public List<Map<String, Object>> getStaffTables(Integer storeId) {
        throw new UnsupportedOperationException("StaffServiceImpl.getStaffTables 尚未实现");
    }

    @Override
    public List<Map<String, Object>> getAlerts(Integer storeId) {
        throw new UnsupportedOperationException("StaffServiceImpl.getAlerts 尚未实现");
    }
}
