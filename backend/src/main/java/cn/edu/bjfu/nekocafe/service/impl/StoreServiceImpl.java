package cn.edu.bjfu.nekocafe.service.impl;

import cn.edu.bjfu.nekocafe.mapper.StoresMapper;
import cn.edu.bjfu.nekocafe.service.StoreService;
import cn.edu.bjfu.nekocafe.vo.StoreVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 门店服务实现
 * 负责人：___（B同学）
 *
 * 当前为临时 mock 版，仅用于验证服务启动和接口联通。
 * B同学接手后需替换为真实数据库查询版本。
 */
@Service
public class StoreServiceImpl implements StoreService {

    @Autowired
    private StoresMapper storesMapper;

    @Override
    public List<StoreVO> listStores() {
        // ====== 临时 mock 数据，B同学接手后删除，改为查 storesMapper ======
        List<StoreVO> list = new ArrayList<>();

        StoreVO s1 = new StoreVO();
        s1.setId(1);
        s1.setName("NekoCafe 朝阳店");
        s1.setAddress("朝阳区三里屯太古里南区 B1-01");
        s1.setDistance(1.2);
        s1.setLat(39.9325);
        s1.setLng(116.4551);
        s1.setAvgPrice(68);
        s1.setRating(4.8);
        s1.setCatCount(12);
        s1.setImageUrl("/uploads/stores/store_1.jpg");
        s1.setTags(Arrays.asList("环境好", "猫咪多", "适合拍照"));
        s1.setOpenTime("10:00 - 22:00");
        s1.setStatus("open");

        StoreVO s2 = new StoreVO();
        s2.setId(2);
        s2.setName("NekoCafe 海淀店");
        s2.setAddress("海淀区中关村大街 1 号");
        s2.setDistance(3.5);
        s2.setLat(39.9836);
        s2.setLng(116.3059);
        s2.setAvgPrice(58);
        s2.setRating(4.5);
        s2.setCatCount(8);
        s2.setImageUrl("/uploads/stores/store_2.jpg");
        s2.setTags(Arrays.asList("安静", "适合办公", "甜点好"));
        s2.setOpenTime("11:00 - 21:00");
        s2.setStatus("open");

        StoreVO s3 = new StoreVO();
        s3.setId(3);
        s3.setName("NekoCafe 西单店");
        s3.setAddress("西城区西单北大街 108 号");
        s3.setDistance(5.0);
        s3.setLat(39.9136);
        s3.setLng(116.3732);
        s3.setAvgPrice(72);
        s3.setRating(4.9);
        s3.setCatCount(15);
        s3.setImageUrl("/uploads/stores/store_3.jpg");
        s3.setTags(Arrays.asList("猫咪亲人", "新品多", "拍照圣地"));
        s3.setOpenTime("09:00 - 23:00");
        s3.setStatus("closed");

        list.add(s1);
        list.add(s2);
        list.add(s3);
        return list;
        // ====== mock 结束 ======
    }
}
