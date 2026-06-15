package cn.edu.bjfu.nekocafe.dto;

/**
 * DTO - 取号请求体（对应接口 J-2）
 */
public class QueueTakeDTO {
    private Integer storeId;
    private Integer persons;
    private String type;  // 桌型，如"双人桌"

    public Integer getStoreId() { return storeId; }
    public void setStoreId(Integer storeId) { this.storeId = storeId; }
    public Integer getPersons() { return persons; }
    public void setPersons(Integer persons) { this.persons = persons; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
