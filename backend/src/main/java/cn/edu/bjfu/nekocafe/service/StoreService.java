package cn.edu.bjfu.nekocafe.service;

import cn.edu.bjfu.nekocafe.vo.StoreVO;
import java.util.List;

/**
 * 门店服务接口
 * 实现类：StoreServiceImpl
 */
public interface StoreService {

    /**
     * 获取所有门店列表（B-1）
     */
    List<StoreVO> listStores();
}
