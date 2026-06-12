package cn.edu.bjfu.nekocafe.service.impl;

import cn.edu.bjfu.nekocafe.entity.TableStatus;
import cn.edu.bjfu.nekocafe.entity.TableStatusExample;
import cn.edu.bjfu.nekocafe.entity.Tables;
import cn.edu.bjfu.nekocafe.entity.TablesExample;
import cn.edu.bjfu.nekocafe.mapper.TableStatusMapper;
import cn.edu.bjfu.nekocafe.mapper.TablesMapper;
import cn.edu.bjfu.nekocafe.service.TableService;
import cn.edu.bjfu.nekocafe.vo.TableVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 桌位服务实现
 * 负责人：B同学
 *
 * 实现接口 C-1：GET /api/tables?storeId={storeId}
 * 查询指定门店的桌位列表，合并实时状态信息
 */
@Service
public class TableServiceImpl implements TableService {

    @Autowired
    private TablesMapper tablesMapper;

    @Autowired
    private TableStatusMapper tableStatusMapper;

    @Override
    public List<TableVO> listTables(Integer storeId) {
        // 1. 查该门店所有活跃桌位
        TablesExample tablesExample = new TablesExample();
        tablesExample.createCriteria()
                .andStoreIdEqualTo(storeId)
                .andIsActiveEqualTo(true);
        List<Tables> tablesList = tablesMapper.selectByExample(tablesExample);

        if (tablesList == null || tablesList.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 批量查询所有桌位的实时状态，构建 tableId → TableStatus 映射
        List<Integer> tableIds = tablesList.stream()
                .map(Tables::getTableId)
                .collect(Collectors.toList());

        TableStatusExample statusExample = new TableStatusExample();
        statusExample.createCriteria().andTableIdIn(tableIds);
        List<TableStatus> statusList = tableStatusMapper.selectByExample(statusExample);

        Map<Integer, TableStatus> statusMap = statusList.stream()
                .collect(Collectors.toMap(TableStatus::getTableId, s -> s, (a, b) -> a));

        // 3. 组装 TableVO
        List<TableVO> result = new ArrayList<>();
        for (Tables t : tablesList) {
            TableVO vo = new TableVO();
            vo.setId(t.getTableId());
            vo.setName(t.getTableNo());
            vo.setType(t.getTableType());
            vo.setCapacity(t.getCapacity());

            vo.setTop(t.getTop());
            vo.setLeft(t.getLeft());
            vo.setWidth(t.getWidth());
            vo.setHeight(t.getHeight());
            // 实时状态映射
            TableStatus ts = statusMap.get(t.getTableId());
            if (ts != null) {
                vo.setStatus(mapTableStatus(ts.getStatus()));
            } else {
                vo.setStatus("available"); // 无状态记录默认可用
            }

            // catType 取自 catTheme（品种名称），catName 暂为空（数据库 tables 表无直接关联猫名）
            vo.setCatType(t.getCatTheme());
            vo.setCatName("");

            // price：包间设附加费 50，其他为 0
            if ("包间".equals(t.getTableType())) {
                vo.setPrice(50);
            } else {
                vo.setPrice(0);
            }

            result.add(vo);
        }

        return result;
    }

    /**
     * 将数据库 table_status 的状态映射为前端 TableVO 所需的状态值
     *
     * DB 状态：IDLE / OCCUPIED / RESERVED / CLEANING
     * 前端要求：available / booked / maintenance
     */
    private String mapTableStatus(String dbStatus) {
        if (dbStatus == null) {
            return "available";
        }
        switch (dbStatus.toUpperCase()) {
            case "IDLE":
                return "available";
            case "OCCUPIED":
            case "RESERVED":
                return "booked";
            case "CLEANING":
                return "maintenance";
            default:
                return "available";
        }
    }
}
