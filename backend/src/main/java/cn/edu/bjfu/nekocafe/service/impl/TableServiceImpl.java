package cn.edu.bjfu.nekocafe.service.impl;

import cn.edu.bjfu.nekocafe.entity.Reservations;
import cn.edu.bjfu.nekocafe.entity.Tables;
import cn.edu.bjfu.nekocafe.entity.TablesExample;
import cn.edu.bjfu.nekocafe.mapper.ReservationsMapper;
import cn.edu.bjfu.nekocafe.mapper.TablesMapper;
import cn.edu.bjfu.nekocafe.service.TableService;
import cn.edu.bjfu.nekocafe.vo.TableVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 桌位服务实现
 * 负责人：B同学
 *
 * 实现接口 C-1：GET /api/tables?storeId={storeId}&reserveDate=...&reserveTime=...&duration=...
 * 查询指定门店的桌位列表，基于时段冲突判断预约状态
 */
@Service
public class TableServiceImpl implements TableService {

    @Autowired
    private TablesMapper tablesMapper;

    @Autowired
    private ReservationsMapper reservationsMapper;

    private static final List<String> ACTIVE_STATUSES = Arrays.asList("BOOKED", "CONFIRMED");

    @Override
    public List<TableVO> listTables(Integer storeId, String reserveDate, String reserveTime, Integer duration) {
        // 1. 查该门店所有活跃桌位
        TablesExample tablesExample = new TablesExample();
        tablesExample.createCriteria()
                .andStoreIdEqualTo(storeId)
                .andIsActiveEqualTo(true);
        List<Tables> tablesList = tablesMapper.selectByExample(tablesExample);

        if (tablesList == null || tablesList.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 时段冲突检查：查该门店所有活跃预约，在内存中判断每张桌子的冲突
        Set<Integer> conflictTableIds = new HashSet<>();
        if (reserveDate != null && reserveTime != null && duration != null && duration > 0) {
            try {
                Date requestStart = parseDateTime(reserveDate, reserveTime);
                Calendar cal = Calendar.getInstance();
                cal.setTime(requestStart);
                cal.add(Calendar.HOUR_OF_DAY, duration);
                Date requestEnd = cal.getTime();

                List<Reservations> activeReservations = reservationsMapper
                        .selectByStoreIdAndStatuses(storeId, ACTIVE_STATUSES);

                if (activeReservations != null) {
                    for (Reservations r : activeReservations) {
                        if (r.getTableId() == null || r.getReservationTime() == null) continue;
                        Date rStart = r.getReservationTime();
                        Calendar rCal = Calendar.getInstance();
                        rCal.setTime(rStart);
                        rCal.add(Calendar.MINUTE, r.getDurationMin() != null ? r.getDurationMin() : 120);
                        Date rEnd = rCal.getTime();

                        // 时段重叠判断：已有开始 < 请求结束 AND 已有结束 > 请求开始
                        if (rStart.before(requestEnd) && rEnd.after(requestStart)) {
                            conflictTableIds.add(r.getTableId());
                        }
                    }
                }
            } catch (Exception e) {
                // 时间解析失败时忽略冲突检查，全部显示 available
            }
        }

        // 3. 组装 TableVO
        List<TableVO> result = new ArrayList<>();
        for (Tables t : tablesList) {
            TableVO vo = new TableVO();
            vo.setId(t.getTableId());
            vo.setName(t.getTableNo());
            vo.setType(t.getTableType());
            vo.setCapacity(t.getCapacity());

            // 实景平面图布局字段
            vo.setTop(t.getTop());
            vo.setLeft(t.getLeft());
            vo.setWidth(t.getWidth());
            vo.setHeight(t.getHeight());

            // 纯基于时段冲突判断状态
            if (reserveDate != null && reserveTime != null && duration != null && duration > 0) {
                vo.setStatus(conflictTableIds.contains(t.getTableId()) ? "booked" : "available");
            } else {
                vo.setStatus("available");
            }

            vo.setCatType(t.getCatTheme());
            vo.setCatName("");

            if ("包间".equals(t.getTableType())) {
                vo.setPrice(50);
            } else {
                vo.setPrice(0);
            }

            result.add(vo);
        }

        return result;
    }

    private Date parseDateTime(String dateStr, String timeStr) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return sdf.parse(dateStr + " " + timeStr);
    }
}
