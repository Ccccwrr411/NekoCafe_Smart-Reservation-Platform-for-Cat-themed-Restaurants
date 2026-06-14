package cn.edu.bjfu.nekocafe.service.impl;

import cn.edu.bjfu.nekocafe.dto.CatHealthRecordDTO;
import cn.edu.bjfu.nekocafe.entity.CatHealthRecords;
import cn.edu.bjfu.nekocafe.entity.CatHealthRecordsExample;
import cn.edu.bjfu.nekocafe.entity.CatProfiles;
import cn.edu.bjfu.nekocafe.entity.CatProfilesExample;
import cn.edu.bjfu.nekocafe.mapper.CatHealthRecordsMapper;
import cn.edu.bjfu.nekocafe.mapper.CatProfilesMapper;
import cn.edu.bjfu.nekocafe.service.CatService;
import cn.edu.bjfu.nekocafe.vo.CatVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 猫咪档案服务实现
 * 负责人：D 同学
 *
 * I-1: listCats       — 获取门店猫咪列表
 * I-2: getCatDetail   — 猫咪详情（含健康记录、疫苗、互动等）
 */
@Service
public class CatServiceImpl implements CatService {

    @Autowired
    private CatProfilesMapper catProfilesMapper;

    @Autowired
    private CatHealthRecordsMapper catHealthRecordsMapper;

    // ==================== I-1 : 猫咪列表 ====================

    @Override
    public List<CatVO> listCats(Integer storeId) {
        // 按门店筛选：storeId 为 null 时返回全部（总部运营）
        CatProfilesExample ex = new CatProfilesExample();
        if (storeId != null) {
            ex.createCriteria().andStoreIdEqualTo(storeId);
        }
        List<CatProfiles> cats = catProfilesMapper.selectByExample(ex);

        List<CatVO> result = new ArrayList<>();
        for (CatProfiles cp : cats) {
            CatVO vo = new CatVO();
            vo.setId(cp.getCatId());
            vo.setName(cp.getName());
            vo.setBreed(cp.getBreed());
            vo.setAge(calcAge(cp.getBirthDate()));
            // 直接使用数据库中的完整头像URL，不再拼接
            vo.setImageUrl(cp.getAvatarUrl());
            if (cp.getWeightKg() != null) {
                vo.setWeight(cp.getWeightKg().doubleValue());
            }
            // personality 是逗号分隔字符串
            if (cp.getPersonality() != null && !cp.getPersonality().isEmpty()) {
                vo.setPersonality(Arrays.asList(cp.getPersonality().split(",")));
            }
            result.add(vo);
        }
        return result;
    }

    // ==================== I-2 : 猫咪详情 ====================

    @Override
    public CatVO getCatDetail(Integer catId) {
        CatProfiles cp = catProfilesMapper.selectByPrimaryKey(catId);
        if (cp == null) return null;

        CatVO vo = new CatVO();

        // --- 列表字段 ---
        vo.setId(cp.getCatId());
        vo.setName(cp.getName());
        vo.setBreed(cp.getBreed());
        vo.setAge(calcAge(cp.getBirthDate()));
        // 直接使用数据库中的完整头像URL，不再拼接
        vo.setImageUrl(cp.getAvatarUrl());
        if (cp.getWeightKg() != null) {
            vo.setWeight(cp.getWeightKg().doubleValue());
        }
        if (cp.getPersonality() != null && !cp.getPersonality().isEmpty()) {
            vo.setPersonality(Arrays.asList(cp.getPersonality().split(",")));
        }

        // --- 详情额外字段 ---
        // 当前体重
        if (cp.getWeightKg() != null) {
            vo.setCurrentWeight(cp.getWeightKg().doubleValue());
        }

        // 理想体重（按品种硬编码）
        vo.setIdealWeight(guessIdealWeight(cp.getBreed()));

        // 查询健康记录
        CatHealthRecordsExample chre = new CatHealthRecordsExample();
        chre.createCriteria().andCatIdEqualTo(catId);
        chre.setOrderByClause("record_date DESC");
        List<CatHealthRecords> records = catHealthRecordsMapper.selectByExample(chre);

        // 按 recordType 分组（数据库中为大写如 VACCINE/WEIGHT/INTERACTION，统一转小写匹配）
        Map<String, List<CatHealthRecords>> grouped = records.stream()
                .filter(r -> r.getRecordType() != null)
                .collect(Collectors.groupingBy(r -> r.getRecordType().toLowerCase()));

        // 体重历史（按日期升序，图表左旧右新）
        List<CatHealthRecords> weightRecords = grouped.getOrDefault("weight", Collections.emptyList());
        if (!weightRecords.isEmpty()) {
            CatVO.WeightHistoryVO wh = new CatVO.WeightHistoryVO();
            List<String> labels = new ArrayList<>();
            List<Double> values = new ArrayList<>();
            // 显式按日期升序排列，不依赖前端翻转
            List<CatHealthRecords> sortedWeight = weightRecords.stream()
                .filter(r -> r.getRecordDate() != null)
                .sorted(Comparator.comparing(CatHealthRecords::getRecordDate))
                .collect(Collectors.toList());
            for (CatHealthRecords r : sortedWeight) {
                labels.add(r.getRecordDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString());
                try {
                    // 兼容 "4.6kg"/"4.6 kg"/"4.6" 等格式
                    String rawVal = r.getRecordValue().replaceAll("(?i)kg", "").trim();
                    values.add(Double.parseDouble(rawVal));
                } catch (NumberFormatException e) {
                    values.add(null);
                }
            }
            wh.setLabels(labels);
            wh.setValues(values);
            vo.setWeightHistory(wh);
        }

        // 疫苗记录
        List<CatHealthRecords> vaccineRecords = grouped.getOrDefault("vaccine", Collections.emptyList());
        List<CatVO.VaccineVO> vaccines = new ArrayList<>();
        for (CatHealthRecords r : vaccineRecords) {
            CatVO.VaccineVO v = new CatVO.VaccineVO();
            v.setName(r.getRecordValue());
            if (r.getRecordDate() != null) {
                v.setDate(r.getRecordDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString());
            }
            // 从 note 字段解析 nextDue（约定格式: nextDue=yyyy-MM-dd）
            if (r.getNote() != null && r.getNote().contains("nextDue=")) {
                String nextDue = r.getNote().replaceAll(".*nextDue=(\\d{4}-\\d{2}-\\d{2}).*", "$1");
                v.setNextDue(nextDue);
                v.setStatus(evalVaccineStatus(nextDue));
            } else {
                // 没有 nextDue 信息，默认有效
                v.setNextDue(null);
                v.setStatus("valid");
            }
            vaccines.add(v);
        }
        vo.setVaccines(vaccines);
        // 最近一次疫苗到期日（列表页展示用）
        if (!vaccines.isEmpty()) {
            vo.setVaccineDue(vaccines.get(0).getNextDue());
        }

        // 互动记录
        List<CatHealthRecords> interactionRecords = grouped.getOrDefault("interaction", Collections.emptyList());
        List<CatVO.InteractionVO> interactions = new ArrayList<>();
        for (CatHealthRecords r : interactionRecords) {
            CatVO.InteractionVO iv = new CatVO.InteractionVO();
            if (r.getRecordDate() != null) {
                iv.setDate(r.getRecordDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString());
            }
            iv.setType(r.getRecordValue());
            // note 格式: "mood=happy|描述文字" 或纯描述文字
            if (r.getNote() != null) {
                if (r.getNote().contains("mood=")) {
                    String moodStr = r.getNote().replaceAll(".*mood=([a-z]+).*", "$1");
                    iv.setMood(moodStr);
                    // desc 为去掉 mood=xxx| 前缀后的描述
                    String desc = r.getNote().replaceAll("mood=[a-z]+\\|?", "").trim();
                    iv.setDesc(desc.isEmpty() ? null : desc);
                } else {
                    iv.setMood("neutral");
                    iv.setDesc(r.getNote());
                }
            } else {
                iv.setMood("neutral");
            }
            interactions.add(iv);
        }
        vo.setInteractions(interactions);

        return vo;
    }

    // ==================== 健康打卡（猫咪管家专用） ====================

    @Override
    public Map<String, Object> addHealthRecord(CatHealthRecordDTO dto) {
        // 参数校验
        if (dto.getCatId() == null) {
            return Map.of("success", false, "message", "catId 不能为空");
        }
        String type = dto.getRecordType();
        if (type == null || (!type.equalsIgnoreCase("WEIGHT")
                && !type.equalsIgnoreCase("VACCINE")
                && !type.equalsIgnoreCase("INTERACTION"))) {
            return Map.of("success", false, "message", "recordType 必须为 WEIGHT / VACCINE / INTERACTION");
        }
        if (dto.getRecordValue() == null || dto.getRecordValue().isBlank()) {
            return Map.of("success", false, "message", "recordValue 不能为空");
        }

        // 构造实体
        CatHealthRecords record = new CatHealthRecords();
        record.setCatId(dto.getCatId());
        record.setRecordType(type.toUpperCase()); // 统一大写存入
        record.setRecordValue(dto.getRecordValue());
        record.setNote(dto.getNote());
        record.setRecordDate(dto.getRecordDate() != null ? dto.getRecordDate() : new Date());
        record.setStaffId(dto.getStaffId());
        record.setCreatedAt(new Date());

        catHealthRecordsMapper.insertSelective(record);

        // 如果是体重记录，同步更新 cat_profiles.weight_kg
        if ("WEIGHT".equalsIgnoreCase(type)) {
            try {
                String rawVal = dto.getRecordValue().replaceAll("(?i)kg", "").trim();
                BigDecimal weightKg = new BigDecimal(rawVal);
                CatProfiles cp = catProfilesMapper.selectByPrimaryKey(dto.getCatId());
                if (cp != null) {
                    cp.setWeightKg(weightKg);
                    catProfilesMapper.updateByPrimaryKeySelective(cp);
                }
            } catch (NumberFormatException ignored) {
                // 体重值格式异常不影响记录插入
            }
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("recordId", record.getRecordId());
        resp.put("message", "打卡成功");
        return resp;
    }

    // ==================== 私有辅助方法 ====================

    /** 由出生日期计算年龄（岁） */
    private Integer calcAge(Date birthDate) {
        if (birthDate == null) return null;
        LocalDate birth = birthDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return Period.between(birth, LocalDate.now()).getYears();
    }

    /** 按品种给出理想体重范围（课设版硬编码） */
    private CatVO.IdealWeightVO guessIdealWeight(String breed) {
        CatVO.IdealWeightVO iw = new CatVO.IdealWeightVO();
        if (breed == null) {
            iw.setMin(3.0);
            iw.setMax(5.0);
        } else {
            switch (breed.toLowerCase()) {
                case "布偶猫": case "ragdoll":
                    iw.setMin(4.0); iw.setMax(9.0); break;
                case "英短": case "british shorthair":
                    iw.setMin(4.0); iw.setMax(8.0); break;
                case "美短": case "american shorthair":
                    iw.setMin(3.5); iw.setMax(7.0); break;
                case "橘猫":
                    iw.setMin(4.0); iw.setMax(10.0); break;
                case "暹罗猫": case "siamese":
                    iw.setMin(2.5); iw.setMax(5.5); break;
                case "缅因猫": case "maine coon":
                    iw.setMin(5.0); iw.setMax(11.0); break;
                default:
                    iw.setMin(3.0); iw.setMax(6.0);
            }
        }
        return iw;
    }

    /** 判断疫苗状态 */
    private String evalVaccineStatus(String nextDue) {
        if (nextDue == null || nextDue.isEmpty()) return "valid";
        try {
            LocalDate due = LocalDate.parse(nextDue);
            LocalDate today = LocalDate.now();
            if (due.isBefore(today)) return "expired";
            if (due.isBefore(today.plusDays(30))) return "expiring";
            return "valid";
        } catch (Exception e) {
            return "valid";
        }
    }
}
