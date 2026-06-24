package cn.edu.bjfu.nekocafe.service.impl;

import cn.edu.bjfu.nekocafe.common.ErrorCode;
import cn.edu.bjfu.nekocafe.entity.*;
import cn.edu.bjfu.nekocafe.exception.BusinessException;
import cn.edu.bjfu.nekocafe.mapper.*;
import cn.edu.bjfu.nekocafe.service.ManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ManagerServiceImpl implements ManagerService {

    @Autowired private StaffSchedulesMapper staffSchedulesMapper;
    @Autowired private StaffShiftsMapper staffShiftsMapper;
    @Autowired private ShiftExceptionsMapper shiftExceptionsMapper;
    @Autowired private UsersMapper usersMapper;
    @Autowired private UserRolesMapper userRolesMapper;

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

    // ─── 辅助：将 "HH:mm" 字符串解析为 Date（用于 time 列）───────────────
    private Date parseTime(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) return null;
        try {
            return new SimpleDateFormat("HH:mm").parse(timeStr);
        } catch (ParseException e) {
            return null;
        }
    }

    // ─── 辅助：将 "yyyy-MM-dd" 字符串解析为 Date（用于 date 列）──────────
    private Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(dateStr);
        } catch (ParseException e) {
            return null;
        }
    }

    // ─── 辅助：将 StaffSchedules 转为前端 Map ────────────────────────────
    private Map<String, Object> scheduleToMap(StaffSchedules s) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat tf = new SimpleDateFormat("HH:mm");
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
        row.put("notes", s.getNotes());
        return row;
    }

    @Override
    @Transactional
    public Map<String, Object> createSchedule(Map<String, Object> body) {
        // 必填参数校验
        Object storeIdObj = body.get("storeId");
        Object staffIdObj = body.get("staffId");
        Object workDateObj = body.get("workDate");
        Object shiftIdObj  = body.get("shiftId");
        if (storeIdObj == null || staffIdObj == null || workDateObj == null || shiftIdObj == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "storeId、staffId、workDate、shiftId 均为必填项");
        }

        Integer storeId = Integer.valueOf(storeIdObj.toString());
        Long staffId    = Long.valueOf(staffIdObj.toString());
        Integer shiftId = Integer.valueOf(shiftIdObj.toString());
        Date workDate   = parseDate(workDateObj.toString());
        if (workDate == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "workDate 格式错误，应为 yyyy-MM-dd");
        }

        // 检查班次是否存在
        StaffShifts shift = staffShiftsMapper.selectByPrimaryKey(shiftId);
        if (shift == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "班次不存在，shiftId=" + shiftId);
        }

        // 构建并插入
        StaffSchedules schedule = new StaffSchedules();
        schedule.setStoreId(storeId);
        schedule.setStaffId(staffId);
        schedule.setWorkDate(workDate);
        schedule.setShiftId(shiftId);
        // startTime / endTime 可由前端覆盖，否则沿用班次定义
        String startTimeStr = body.get("startTime") != null ? body.get("startTime").toString() : null;
        String endTimeStr   = body.get("endTime")   != null ? body.get("endTime").toString()   : null;
        schedule.setStartTime(startTimeStr != null ? parseTime(startTimeStr) : shift.getStartTime());
        schedule.setEndTime(endTimeStr     != null ? parseTime(endTimeStr)   : shift.getEndTime());
        // 可选字段
        if (body.get("position") != null) schedule.setPosition(body.get("position").toString());
        if (body.get("notes")    != null) schedule.setNotes(body.get("notes").toString());
        Date now = new Date();
        schedule.setCreatedAt(now);
        schedule.setUpdatedAt(now);

        staffSchedulesMapper.insertSelective(schedule);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("message", "排班创建成功");
        result.put("schedule", scheduleToMap(schedule));
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> updateSchedule(Long scheduleId, Map<String, Object> body) {
        StaffSchedules existing = staffSchedulesMapper.selectByPrimaryKey(scheduleId);
        if (existing == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "排班记录不存在，scheduleId=" + scheduleId);
        }

        // 有哪些字段就更新哪些字段
        if (body.get("workDate") != null) {
            Date workDate = parseDate(body.get("workDate").toString());
            if (workDate == null) throw new BusinessException(ErrorCode.BAD_REQUEST, "workDate 格式错误");
            existing.setWorkDate(workDate);
        }
        if (body.get("shiftId") != null) {
            Integer shiftId = Integer.valueOf(body.get("shiftId").toString());
            StaffShifts shift = staffShiftsMapper.selectByPrimaryKey(shiftId);
            if (shift == null) throw new BusinessException(ErrorCode.NOT_FOUND, "班次不存在，shiftId=" + shiftId);
            existing.setShiftId(shiftId);
            // 若同时没有覆盖 startTime/endTime，则沿用新班次时间
            if (body.get("startTime") == null) existing.setStartTime(shift.getStartTime());
            if (body.get("endTime")   == null) existing.setEndTime(shift.getEndTime());
        }
        if (body.get("startTime") != null) {
            existing.setStartTime(parseTime(body.get("startTime").toString()));
        }
        if (body.get("endTime") != null) {
            existing.setEndTime(parseTime(body.get("endTime").toString()));
        }
        if (body.get("staffId") != null) {
            existing.setStaffId(Long.valueOf(body.get("staffId").toString()));
        }
        if (body.get("position") != null) {
            existing.setPosition(body.get("position").toString());
        }
        if (body.get("notes") != null) {
            existing.setNotes(body.get("notes").toString());
        }
        existing.setUpdatedAt(new Date());

        staffSchedulesMapper.updateByPrimaryKeySelective(existing);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("message", "排班更新成功");
        result.put("schedule", scheduleToMap(existing));
        return result;
    }

    @Override
    public List<Map<String, Object>> searchStaff(Integer storeId, String keyword) {
        // 1. 从 user_roles 表查出该门店的所有 userId
        UserRolesExample urEx = new UserRolesExample();
        urEx.createCriteria().andStoreIdEqualTo(storeId);
        List<UserRoles> roles = userRolesMapper.selectByExample(urEx);
        if (roles == null || roles.isEmpty()) return Collections.emptyList();

        Set<Long> storeUserIds = new LinkedHashSet<>();
        for (UserRoles r : roles) storeUserIds.add(r.getUserId());

        // 2. 查这些用户，若有关键词则按 real_name / nickname 模糊过滤
        UsersExample uEx = new UsersExample();
        UsersExample.Criteria c = uEx.createCriteria();
        c.andUserIdIn(new ArrayList<>(storeUserIds));
        if (keyword != null && !keyword.trim().isEmpty()) {
            String like = "%" + keyword.trim() + "%";
            // 需要同时匹配 realName 或 nickname → 用 or
            UsersExample.Criteria c2 = uEx.createCriteria();
            c2.andUserIdIn(new ArrayList<>(storeUserIds));
            c2.andRealNameLike(like);
            UsersExample.Criteria c3 = uEx.createCriteria();
            c3.andUserIdIn(new ArrayList<>(storeUserIds));
            c3.andNicknameLike(like);
            uEx.or(c2);
            uEx.or(c3);
        }
        uEx.setOrderByClause("real_name ASC, nickname ASC");
        List<Users> users = usersMapper.selectByExample(uEx);

        // 3. 组装结果
        List<Map<String, Object>> res = new ArrayList<>();
        if (users != null) {
            for (Users u : users) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("userId", u.getUserId());
                // 显示名：realName 优先，否则 nickname
                String displayName = u.getRealName();
                if (displayName == null || displayName.isEmpty()) displayName = u.getNickname();
                if (displayName == null || displayName.isEmpty()) displayName = "用户#" + u.getUserId();
                row.put("displayName", displayName);
                row.put("phone", u.getPhone());
                res.add(row);
            }
        }
        return res;
    }
}