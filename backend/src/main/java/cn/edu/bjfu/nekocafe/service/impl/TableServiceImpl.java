package cn.edu.bjfu.nekocafe.service.impl;

import cn.edu.bjfu.nekocafe.mapper.TablesMapper;
import cn.edu.bjfu.nekocafe.mapper.TableStatusMapper;
import cn.edu.bjfu.nekocafe.service.TableService;
import cn.edu.bjfu.nekocafe.vo.TableVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 桌位服务实现
 * 负责人：___
 *
 * 实现要点：
 *   1. 查 tables 表（storeId = ? AND isActive = true）
 *   2. LEFT JOIN table_status 获取实时状态（status: available/booked/maintenance）
 *   3. 联查 cat_profiles 获取 catName（可选，若 catTheme 字段已含品种信息可直接用）
 *   4. price 字段：包间（tableType='包间'）可设固定附加费，其他为 0
 */
@Service
public class TableServiceImpl implements TableService {

    @Autowired
    private TablesMapper tablesMapper;

    @Autowired
    private TableStatusMapper tableStatusMapper;

    @Override
    public List<TableVO> listTables(Integer storeId) {
        // TODO
        throw new UnsupportedOperationException("TableServiceImpl.listTables 尚未实现");
    }
}
