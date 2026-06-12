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
     * @param storeId 门店 ID
     */
    List<TableVO> listTables(Integer storeId);
}
