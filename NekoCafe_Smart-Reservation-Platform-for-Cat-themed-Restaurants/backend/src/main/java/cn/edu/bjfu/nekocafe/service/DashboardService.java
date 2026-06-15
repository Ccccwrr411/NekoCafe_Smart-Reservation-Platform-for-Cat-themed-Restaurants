package cn.edu.bjfu.nekocafe.service;

import cn.edu.bjfu.nekocafe.vo.DashboardMetricsVO;

public interface DashboardService {
    DashboardMetricsVO getMetrics(Integer storeId, String range);
}
