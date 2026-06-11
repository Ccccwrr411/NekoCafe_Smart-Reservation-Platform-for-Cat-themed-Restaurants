package cn.edu.bjfu.nekocafe.service.impl;

import cn.edu.bjfu.nekocafe.mapper.DishesMapper;
import cn.edu.bjfu.nekocafe.mapper.StoreDishesMapper;
import cn.edu.bjfu.nekocafe.service.MenuService;
import cn.edu.bjfu.nekocafe.vo.MenuVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 菜单服务实现
 * 负责人：___
 *
 * 实现要点：
 *   1. 查 store_dishes 表筛选该门店的菜品 ID
 *   2. JOIN dishes 表获取菜品详情
 *   3. 查 dishes 表的 category 字段，提取去重后的分类列表
 *      注意：dishes 表目前无独立 category 表，需要从 Dishes.category 字段聚合
 *   4. imageUrl: "http://host/uploads/menu/item_{id}.jpg"
 */
@Service
public class MenuServiceImpl implements MenuService {

    @Autowired
    private DishesMapper dishesMapper;

    @Autowired
    private StoreDishesMapper storeDishesMapper;

    @Override
    public MenuVO getMenu(Integer storeId) {
        // TODO
        throw new UnsupportedOperationException("MenuServiceImpl.getMenu 尚未实现");
    }
}
