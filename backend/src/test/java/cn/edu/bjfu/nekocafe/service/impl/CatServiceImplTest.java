package cn.edu.bjfu.nekocafe.service.impl;

import cn.edu.bjfu.nekocafe.dto.CatHealthRecordDTO;
import cn.edu.bjfu.nekocafe.entity.CatHealthRecords;
import cn.edu.bjfu.nekocafe.entity.CatProfiles;
import cn.edu.bjfu.nekocafe.mapper.CatHealthRecordsMapper;
import cn.edu.bjfu.nekocafe.mapper.CatProfilesMapper;
import cn.edu.bjfu.nekocafe.vo.CatVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatServiceImplTest {

  @Mock private CatProfilesMapper catProfilesMapper;
  @Mock private CatHealthRecordsMapper catHealthRecordsMapper;

  @InjectMocks
  private CatServiceImpl service;

  @Test
  @DisplayName("测试：获取猫咪列表")
  void listCats_Success() {
    CatProfiles cat = new CatProfiles();
    cat.setCatId(1);
    cat.setName("咪咪");
    cat.setPersonality("高冷,贪吃");
    cat.setWeightKg(new BigDecimal("4.5"));

    // 伪造两年前出生的日期，测试 calcAge 方法
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.YEAR, -2);
    cat.setBirthDate(cal.getTime());

    when(catProfilesMapper.selectByExample(any())).thenReturn(Arrays.asList(cat));

    List<CatVO> result = service.listCats(1);
    assertFalse(result.isEmpty());
    assertEquals(2, result.get(0).getAge()); // 验证年龄计算
    assertEquals(2, result.get(0).getPersonality().size()); // 验证逗号分隔解析
  }

  @Test
  @DisplayName("测试：获取猫咪详情 (疯狂拉满所有健康记录的解析分支)")
  void getCatDetail_Success() {
    CatProfiles cat = new CatProfiles();
    cat.setCatId(1);
    cat.setBreed("布偶猫"); // 测试 guessIdealWeight 的 switch 分支
    cat.setWeightKg(new BigDecimal("4.5"));
    when(catProfilesMapper.selectByPrimaryKey(1)).thenReturn(cat);

    // 伪造 3 种不同类型的健康记录，让所有的 if (type.equals) 全部亮绿灯
    CatHealthRecords r1 = new CatHealthRecords();
    r1.setRecordType("WEIGHT");
    r1.setRecordValue("4.6kg"); // 测试正则替换 "kg"
    r1.setRecordDate(new Date());

    CatHealthRecords r2 = new CatHealthRecords();
    r2.setRecordType("VACCINE");
    r2.setRecordValue("猫三联");
    r2.setNote("nextDue=2020-01-01"); // 测试疫苗过期状态解析

    CatHealthRecords r3 = new CatHealthRecords();
    r3.setRecordType("INTERACTION");
    r3.setRecordValue("玩逗猫棒");
    r3.setNote("mood=happy|今天玩得很开心"); // 测试心情与描述解析
    r3.setRecordDate(new Date());

    when(catHealthRecordsMapper.selectByExample(any())).thenReturn(Arrays.asList(r1, r2, r3));

    CatVO result = service.getCatDetail(1);

    assertNotNull(result);
    assertEquals(4.0, result.getIdealWeight().getMin()); // 布偶猫的理想体重下限
    assertNotNull(result.getWeightHistory());
    assertEquals(1, result.getVaccines().size());
    assertEquals("expired", result.getVaccines().get(0).getStatus()); // 2020年肯定过期了
    assertEquals("happy", result.getInteractions().get(0).getMood());
    assertEquals("今天玩得很开心", result.getInteractions().get(0).getDesc());
  }

  @Test
  @DisplayName("测试：新增健康记录 (体重记录需同步更新 Profile)")
  void addHealthRecord_Weight_Success() {
    CatHealthRecordDTO dto = new CatHealthRecordDTO();
    dto.setCatId(1);
    dto.setRecordType("WEIGHT");
    dto.setRecordValue("4.8kg");

    CatProfiles cat = new CatProfiles();
    cat.setCatId(1);
    when(catProfilesMapper.selectByPrimaryKey(1)).thenReturn(cat);

    lenient().when(catHealthRecordsMapper.insertSelective(any())).thenReturn(1);
    lenient().when(catProfilesMapper.updateByPrimaryKeySelective(any())).thenReturn(1);

    Map<String, Object> result = service.addHealthRecord(dto);
    assertTrue((Boolean) result.get("success"));

    // 验证确实调用了更新猫咪资料的方法（同步体重）
    verify(catProfilesMapper, times(1)).updateByPrimaryKeySelective(any());
  }

  @Test
  @DisplayName("测试：新增健康记录 (异常拦截)")
  void addHealthRecord_Invalid() {
    CatHealthRecordDTO dto = new CatHealthRecordDTO();
    dto.setCatId(1);
    dto.setRecordType("ERROR_TYPE"); // 错误类型

    Map<String, Object> result = service.addHealthRecord(dto);
    assertFalse((Boolean) result.get("success"));
    assertTrue(result.get("message").toString().contains("必须为"));
  }
}
