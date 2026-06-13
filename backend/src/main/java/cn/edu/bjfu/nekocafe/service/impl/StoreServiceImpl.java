package cn.edu.bjfu.nekocafe.service.impl;

import cn.edu.bjfu.nekocafe.entity.Stores;
import cn.edu.bjfu.nekocafe.entity.StoresExample;
import cn.edu.bjfu.nekocafe.mapper.StoresMapper;
import cn.edu.bjfu.nekocafe.service.StoreService;
import cn.edu.bjfu.nekocafe.vo.StoreVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 门店服务实现
 * 负责人：B同学
 *
 * 实现接口 B-1：GET /api/stores
 * 从数据库查询所有门店，按距离排序后返回
 */
@Service
public class StoreServiceImpl implements StoreService {

    private static final String IMAGE_BASE_URL = "http://172.20.10.3:8081";

    @Autowired
    private StoresMapper storesMapper;

    @Override
    public List<StoreVO> listStores() {
        // 查询所有营业中的门店（status = 1）
        StoresExample example = new StoresExample();
        example.createCriteria().andStatusEqualTo((short) 1);
        List<Stores> storesList = storesMapper.selectByExample(example);

        // 如果数据库中暂无数据，回退查询所有门店
        if (storesList == null || storesList.isEmpty()) {
            storesList = storesMapper.selectByExample(null);
        }

        List<StoreVO> result = new ArrayList<>();
        if (storesList == null) {
            return result;
        }

        for (Stores s : storesList) {
            StoreVO vo = new StoreVO();
            vo.setId(s.getStoreId());
            vo.setName(s.getName());
            vo.setAddress(s.getAddress());

            // 经纬度：BigDecimal → Double
            if (s.getLongitude() != null) {
                vo.setLng(s.getLongitude().doubleValue());
            }
            if (s.getLatitude() != null) {
                vo.setLat(s.getLatitude().doubleValue());
            }

            // distance：当前接口无用户坐标参数，暂设为 0
            // 后期可在接口增加 userLat/userLng 参数后用 Haversine 公式计算
            vo.setDistance(0.0);

            // status 转换：Short 1 → "open"，0 → "closed"
            vo.setStatus(s.getStatus() != null && s.getStatus() == 1 ? "open" : "closed");

            // 营业时间：直接使用 businessHours
            vo.setOpenTime(s.getBusinessHours());

            // 图片：直接使用数据库中的完整 URL
            vo.setImageUrl(s.getImageUrl());

            // 以下字段在 stores 表中无直接对应，暂设默认值
            // avgPrice / rating / catCount / tags 可通过联查 reviews / cat_profiles / dishes 计算
            vo.setAvgPrice(0);
            vo.setRating(5.0);
            vo.setCatCount(0);
            vo.setTags(new ArrayList<>());

            result.add(vo);
        }

        return result;
    }
}
