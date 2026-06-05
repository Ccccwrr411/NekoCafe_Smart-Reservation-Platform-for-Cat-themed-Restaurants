package cn.edu.bjfu.nekocafe.service;

import cn.edu.bjfu.nekocafe.vo.CatVO;
import java.util.List;
import java.util.Map;

/**
 * 猫咪档案服务接口
 * 实现类：CatServiceImpl
 */
public interface CatService {

    /**
     * 获取指定门店的猫咪列表（I-1）
     */
    List<CatVO> listCats(Integer storeId);

    /**
     * 获取猫咪详情（I-2）
     * 包含健康记录、疫苗、互动记录等
     */
    CatVO getCatDetail(Integer catId);
}
