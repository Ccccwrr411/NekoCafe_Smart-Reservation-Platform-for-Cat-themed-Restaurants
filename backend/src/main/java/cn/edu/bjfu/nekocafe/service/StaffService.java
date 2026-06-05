package cn.edu.bjfu.nekocafe.service;

import cn.edu.bjfu.nekocafe.vo.DashboardMetricsVO;
import cn.edu.bjfu.nekocafe.vo.TableVO;
import java.util.List;
import java.util.Map;

/**
 * 店员 & 数据看板服务接口
 * 实现类：StaffServiceImpl
 */
public interface StaffService {

    /**
     * 获取运营指标数据（K-1）
     *
     * @param storeId 门店 ID
     * @param range   时间范围：7d / 30d / 90d
     */
    DashboardMetricsVO getDashboardMetrics(Integer storeId, String range);

    /**
     * 店员端桌位实时状态（L-1）
     * 包含 customer / arriveTime / estLeaveTime 等运营字段
     */
    List<Map<String, Object>> getStaffTables(Integer storeId);

    /**
     * 获取异常告警列表（L-2）
     * 如超时、未到店、设备故障等
     */
    List<Map<String, Object>> getAlerts(Integer storeId);
}
