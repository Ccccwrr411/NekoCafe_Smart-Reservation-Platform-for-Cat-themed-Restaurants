package cn.edu.bjfu.nekocafe.service;

import cn.edu.bjfu.nekocafe.vo.TableVO;
import java.util.List;

/**
 * 桌位服务接口
 * 实现类：TableServiceImpl
 */
public interface TableService {

    /**
     * 获取指定门店的桌位列表（C-1）
     *
     * @param storeId     门店 ID
     * @param reserveDate 预约日期（可选，用于时段冲突判断）
     * @param reserveTime 预约时间（可选，格式 HH:mm）
     * @param duration    预约时长（可选，单位小时）
     */
    List<TableVO> listTables(Integer storeId, String reserveDate, String reserveTime, Integer duration);
}
