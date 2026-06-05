package cn.edu.bjfu.nekocafe.service;

import cn.edu.bjfu.nekocafe.vo.MenuVO;

/**
 * 菜单服务接口
 * 实现类：MenuServiceImpl
 */
public interface MenuService {

    /**
     * 获取指定门店的菜单（D-1）
     * 返回分类列表 + 菜品列表
     *
     * @param storeId 门店 ID
     */
    MenuVO getMenu(Integer storeId);
}
