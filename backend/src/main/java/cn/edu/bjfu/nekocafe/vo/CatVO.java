package cn.edu.bjfu.nekocafe.vo;

import java.util.List;

/**
 * VO - 猫咪列表 & 详情响应体（对应接口 I-1 / I-2）
 * entity CatProfiles + CatHealthRecords 联查组装
 */
public class CatVO {
    // --- 列表字段 ---
    private Integer id;
    private String name;
    private String breed;
    private Integer age;          // 由 birthDate 计算
    private String gender;
    private String imageUrl;
    private Double weight;
    private String desc;
    private List<String> personality;  // CatProfiles.personality 字段按逗号分割
    private String vaccineDue;

    // --- 详情额外字段 ---
    private Double currentWeight;
    private IdealWeightVO idealWeight;
    private WeightHistoryVO weightHistory;
    private List<VaccineVO> vaccines;
    private List<InteractionVO> interactions;
    private Integer healthScore;
    private String healthAdvice;

    public static class IdealWeightVO {
        private Double min;
        private Double max;
        public Double getMin() { return min; }
        public void setMin(Double min) { this.min = min; }
        public Double getMax() { return max; }
        public void setMax(Double max) { this.max = max; }
    }

    public static class WeightHistoryVO {
        private List<String> labels;
        private List<Double> values;
        public List<String> getLabels() { return labels; }
        public void setLabels(List<String> labels) { this.labels = labels; }
        public List<Double> getValues() { return values; }
        public void setValues(List<Double> values) { this.values = values; }
    }

    public static class VaccineVO {
        private String name;
        private String date;
        private String nextDue;
        private String status;  // valid / expiring / expired
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public String getNextDue() { return nextDue; }
        public void setNextDue(String nextDue) { this.nextDue = nextDue; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class InteractionVO {
        private String date;
        private String type;
        private String desc;
        private String mood;  // happy / neutral / grumpy
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getDesc() { return desc; }
        public void setDesc(String desc) { this.desc = desc; }
        public String getMood() { return mood; }
        public void setMood(String mood) { this.mood = mood; }
    }

    // ---- Getters & Setters ----
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBreed() { return breed; }
    public void setBreed(String breed) { this.breed = breed; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }
    public String getDesc() { return desc; }
    public void setDesc(String desc) { this.desc = desc; }
    public List<String> getPersonality() { return personality; }
    public void setPersonality(List<String> personality) { this.personality = personality; }
    public String getVaccineDue() { return vaccineDue; }
    public void setVaccineDue(String vaccineDue) { this.vaccineDue = vaccineDue; }
    public Double getCurrentWeight() { return currentWeight; }
    public void setCurrentWeight(Double currentWeight) { this.currentWeight = currentWeight; }
    public IdealWeightVO getIdealWeight() { return idealWeight; }
    public void setIdealWeight(IdealWeightVO idealWeight) { this.idealWeight = idealWeight; }
    public WeightHistoryVO getWeightHistory() { return weightHistory; }
    public void setWeightHistory(WeightHistoryVO weightHistory) { this.weightHistory = weightHistory; }
    public List<VaccineVO> getVaccines() { return vaccines; }
    public void setVaccines(List<VaccineVO> vaccines) { this.vaccines = vaccines; }
    public List<InteractionVO> getInteractions() { return interactions; }
    public void setInteractions(List<InteractionVO> interactions) { this.interactions = interactions; }
    public Integer getHealthScore() { return healthScore; }
    public void setHealthScore(Integer healthScore) { this.healthScore = healthScore; }
    public String getHealthAdvice() { return healthAdvice; }
    public void setHealthAdvice(String healthAdvice) { this.healthAdvice = healthAdvice; }
}
