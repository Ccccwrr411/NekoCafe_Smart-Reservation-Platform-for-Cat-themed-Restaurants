package cn.edu.bjfu.nekocafe.service.impl;

import cn.edu.bjfu.nekocafe.entity.CatHealthRecords;
import cn.edu.bjfu.nekocafe.entity.CatHealthRecordsExample;
import cn.edu.bjfu.nekocafe.entity.CatProfiles;
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
 *
 * 注意：当前 cat_profiles 表没有 store_id 字段，因此 listCats 暂返回全部猫咪。
 *       后续如需按门店筛选，请在 cat_profiles 新增 store_id INT 字段并修改此处。
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
        // TODO: cat_profiles 表当前无 store_id 字段，暂返回全部猫咪
        // 如需按门店筛选: CatProfilesExample ex = new CatProfilesExample();
        //                     ex.createCriteria().andStoreIdEqualTo(storeId);
        List<CatProfiles> cats = catProfilesMapper.selectByExample(null);

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

        // 按 recordType 分组
        Map<String, List<CatHealthRecords>> grouped = records.stream()
                .filter(r -> r.getRecordType() != null)
                .collect(Collectors.groupingBy(CatHealthRecords::getRecordType));

        // 体重历史
        List<CatHealthRecords> weightRecords = grouped.getOrDefault("weight", Collections.emptyList());
        if (!weightRecords.isEmpty()) {
            CatVO.WeightHistoryVO wh = new CatVO.WeightHistoryVO();
            List<String> labels = new ArrayList<>();
            List<Double> values = new ArrayList<>();
            for (CatHealthRecords r : weightRecords) {
                if (r.getRecordDate() != null) {
                    labels.add(r.getRecordDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString());
                }
                try {
                    values.add(Double.parseDouble(r.getRecordValue()));
                } catch (NumberFormatException e) {
                    values.add(null);
                }
            }
            Collections.reverse(labels); // 按时间升序
            Collections.reverse(values);
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
                v.setNextDue(r.getNote().replace("nextDue=", "").trim());
                v.setStatus(evalVaccineStatus(v.getNextDue()));
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
            iv.setDesc(r.getNote());
            // mood 由 note 里的 mood=xxx 解析
            if (r.getNote() != null && r.getNote().contains("mood=")) {
                String moodStr = r.getNote().replaceAll(".*mood=([a-z]+).*", "$1");
                iv.setMood(moodStr);
            } else {
                iv.setMood("neutral");
            }
            interactions.add(iv);
        }
        vo.setInteractions(interactions);

        // 健康评分：综合体重偏离 + 疫苗状态
        int score = calcHealthScore(vo.getCurrentWeight(), vo.getIdealWeight(), vaccines);
        vo.setHealthScore(score);
        vo.setHealthAdvice(genHealthAdvice(score, vaccines));

        return vo;
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

    /** 计算健康评分 (0-100)，体重 40 分 + 疫苗 60 分 */
    private int calcHealthScore(Double currentWeight, CatVO.IdealWeightVO ideal, List<CatVO.VaccineVO> vaccines) {
        int score = 100;

        // 体重评分
        if (currentWeight != null && ideal != null && ideal.getMin() != null && ideal.getMax() != null) {
            double min = ideal.getMin();
            double max = ideal.getMax();
            if (currentWeight < min) {
                score -= (int) ((min - currentWeight) / min * 40);
            } else if (currentWeight > max) {
                score -= (int) ((currentWeight - max) / max * 40);
            }
        }

        // 疫苗评分
        long expired = vaccines.stream().filter(v -> "expired".equals(v.getStatus())).count();
        long expiring = vaccines.stream().filter(v -> "expiring".equals(v.getStatus())).count();
        score -= expired * 30 + expiring * 10;

        return Math.max(0, Math.min(100, score));
    }

    /** 根据评分和疫苗状态生成健康建议 */
    private String genHealthAdvice(int score, List<CatVO.VaccineVO> vaccines) {
        StringBuilder sb = new StringBuilder();
        if (score >= 90) {
            sb.append("猫咪状态良好，继续保持当前护理方案。");
        } else if (score >= 70) {
            sb.append("猫咪健康状况一般，建议关注体重管理和疫苗计划。");
        } else {
            sb.append("猫咪健康需要关注，请尽快安排体检。");
        }
        boolean hasExpired = vaccines.stream().anyMatch(v -> "expired".equals(v.getStatus()));
        boolean hasExpiring = vaccines.stream().anyMatch(v -> "expiring".equals(v.getStatus()));
        if (hasExpired) {
            sb.append(" 有疫苗已过期，请尽快补种。");
        } else if (hasExpiring) {
            sb.append(" 有疫苗即将到期，请安排补种。");
        }
        return sb.toString();
    }
}
