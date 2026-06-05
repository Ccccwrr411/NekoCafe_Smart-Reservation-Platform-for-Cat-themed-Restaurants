package cn.edu.bjfu.nekocafe.service.impl;

import cn.edu.bjfu.nekocafe.mapper.CatHealthRecordsMapper;
import cn.edu.bjfu.nekocafe.mapper.CatProfilesMapper;
import cn.edu.bjfu.nekocafe.service.CatService;
import cn.edu.bjfu.nekocafe.vo.CatVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 猫咪档案服务实现
 * 负责人：___
 *
 * 注意：CatProfiles 表中的 userId 是该猫咪的"主人用户"（此为用户自己的猫），
 * 但接口要求的是门店内的猫，需要确认数据库是否有 storeId 关联。
 * 如无，可在 cat_profiles 表新增 store_id 字段，或新建 store_cats 关联表。
 *
 * 实现要点：
 *   listCats：查指定 storeId 的猫咪列表，personality 字段按逗号分割为 List<String>
 *   getCatDetail：追加 CatHealthRecords 中的体重历史和疫苗记录
 *     healthScore：可根据最近体重是否在 idealWeight 范围内，以及疫苗是否过期来计算
 *     imageUrl：列表图 "http://host/uploads/cats/cat_{id}.jpg"
 *               详情大图 "http://host/uploads/cats/cat_{id}_detail.jpg"
 */
@Service
public class CatServiceImpl implements CatService {

    @Autowired
    private CatProfilesMapper catProfilesMapper;

    @Autowired
    private CatHealthRecordsMapper catHealthRecordsMapper;

    @Override
    public List<CatVO> listCats(Integer storeId) {
        throw new UnsupportedOperationException("CatServiceImpl.listCats 尚未实现");
    }

    @Override
    public CatVO getCatDetail(Integer catId) {
        throw new UnsupportedOperationException("CatServiceImpl.getCatDetail 尚未实现");
    }
}
