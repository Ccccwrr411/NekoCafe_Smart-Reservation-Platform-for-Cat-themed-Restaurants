package cn.edu.bjfu.nekocafe.service;

import java.util.List;
import java.util.Map;

/**
 * 店长管理服务接口
 * 排班 / 班次 / 异常申请 / 审批
 */
public interface ManagerService {

    /** 获取门店排班列表 */
    List<Map<String, Object>> getSchedules(Integer storeId);

    /** 获取所有班次定义 */
    List<Map<String, Object>> getShifts();

    /** 获取门店异常申请列表 */
    List<Map<String, Object>> getExceptions(Integer storeId);

    /** 审批异常申请 */
    Map<String, Object> reviewException(Long exceptionId, String action);

    /** 创建排班 */
    Map<String, Object> createSchedule(Map<String, Object> body);

    /** 修改排班 */
    Map<String, Object> updateSchedule(Long scheduleId, Map<String, Object> body);

    /** 按门店搜索员工（支持姓名/昵称模糊搜索） */
    List<Map<String, Object>> searchStaff(Integer storeId, String keyword);
}
