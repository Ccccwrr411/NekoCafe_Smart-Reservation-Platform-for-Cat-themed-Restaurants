package cn.edu.bjfu.nekocafe.service.impl;

import cn.edu.bjfu.nekocafe.common.ErrorCode;
import cn.edu.bjfu.nekocafe.entity.*;
import cn.edu.bjfu.nekocafe.exception.BusinessException;
import cn.edu.bjfu.nekocafe.mapper.*;
import cn.edu.bjfu.nekocafe.service.ManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ManagerServiceImpl implements ManagerService {

    @Autowired private StaffSchedulesMapper staffSchedulesMapper;
    @Autowired private StaffShiftsMapper staffShiftsMapper;
    @Autowired private ShiftExceptionsMapper shiftExceptionsMapper;
    @Autowired private UsersMapper usersMapper;

    private String resolveStaffName(Long staffId) {
        if (staffId == null) return "未知员工";
        try { Users u = usersMapper.selectByPrimaryKey(staffId);
            if (u != null) {
                String rn = u.getRealName();
                if (rn != null && !rn.isEmpty()) return rn;
                String nn = u.getNickname();
                if (nn != null && !nn.isEmpty()) return nn;
            }
        } catch (Exception ignored) {}
        return "员工#" + staffId;
    }

    @Override
    public List<Map<String, Object>> getSchedules(Integer storeId) {
        StaffSchedulesExample ex = new StaffSchedulesExample();
        ex.setOrderByClause("work_date ASC, start_time ASC");
        ex.createCriteria().andStoreIdEqualTo(storeId);
        List<StaffSchedules> list = staffSchedulesMapper.selectByExample(ex);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat tf = new SimpleDateFormat("HH:mm");
        List<Map<String, Object>> res = new ArrayList<>();
        if (list != null) {
            for (StaffSchedules s : list) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("scheduleId", s.getScheduleId());
                row.put("staffId", s.getStaffId());
                row.put("staffName", resolveStaffName(s.getStaffId()));
                row.put("workDate", s.getWorkDate() != null ? df.format(s.getWorkDate()) : null);
                row.put("startTime", s.getStartTime() != null ? tf.format(s.getStartTime()) : null);
                row.put("endTime", s.getEndTime() != null ? tf.format(s.getEndTime()) : null);
                row.put("position", s.getPosition());
                row.put("shiftId", s.getShiftId());
                String sn = "";
                if (s.getShiftId() != null) {
                    try {
                        StaffShifts sh = staffShiftsMapper.selectByPrimaryKey(s.getShiftId());
                        if (sh != null) sn = sh.getShiftName();
                    } catch (Exception ignored) {}
                }
                row.put("shiftName", sn);
                res.add(row);
            }
        }
        return res;
    }

    @Override
    public List<Map<String, Object>> getShifts() {
        StaffShiftsExample ex = new StaffShiftsExample();
        ex.setOrderByClause("shift_id ASC");
        List<StaffShifts> list = staffShiftsMapper.selectByExample(ex);
        SimpleDateFormat tf = new SimpleDateFormat("HH:mm");
        List<Map<String, Object>> res = new ArrayList<>();
        if (list != null) for (StaffShifts s : list) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("shiftId", s.getShiftId());
            row.put("shiftName", s.getShiftName());
            row.put("startTime", s.getStartTime() != null ? tf.format(s.getStartTime()) : null);
            row.put("endTime", s.getEndTime() != null ? tf.format(s.getEndTime()) : null);
            res.add(row);
        }
        return res;
    }

    @Override
    public List<Map<String, Object>> getExceptions(Integer storeId) {
        ShiftExceptionsExample ex = new ShiftExceptionsExample();
        ex.setOrderByClause("created_at DESC");
        ex.createCriteria().andStoreIdEqualTo(storeId);
        List<ShiftExceptions> list = shiftExceptionsMapper.selectByExample(ex);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat dtf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        List<Map<String, Object>> res = new ArrayList<>();
        if (list != null) for (ShiftExceptions e : list) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("exceptionId", e.getExceptionId());
            row.put("staffId", e.getStaffId());
            row.put("staffName", resolveStaffName(e.getStaffId()));
            row.put("type", e.getType());
            row.put("status", e.getStatus());
            row.put("exceptionDate", e.getExceptionDate() != null ? df.format(e.getExceptionDate()) : null);
            row.put("reason", e.getReason());
            row.put("createdAt", e.getCreatedAt() != null ? dtf.format(e.getCreatedAt()) : null);
            res.add(row);
        }
        return res;
    }

    @Override
    @Transactional
    public Map<String, Object> reviewException(Long exceptionId, String action) {
        ShiftExceptions ex = shiftExceptionsMapper.selectByPrimaryKey(exceptionId);
        if (ex == null) throw new BusinessException(ErrorCode.NOT_FOUND, "异常申请不存在");
        if (!"PENDING".equalsIgnoreCase(ex.getStatus()))
            throw new BusinessException(ErrorCode.BAD_REQUEST, "该申请已处理，无法重复审批");
        String newStatus;
        String message;
        if ("approve".equalsIgnoreCase(action)) {
            newStatus = "APPROVED";
            message = "审批通过";
        } else if ("reject".equalsIgnoreCase(action)) {
            newStatus = "REJECTED";
            message = "审批驳回";
        } else {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "无效的审批操作，仅支持 approve 或 reject");
        }
        ex.setStatus(newStatus);
        shiftExceptionsMapper.updateByPrimaryKeySelective(ex);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("message", message);
        result.put("status", newStatus);
        return result;
    }
}