package cn.edu.bjfu.nekocafe.dto;

import java.util.Date;

/**
 * 猫咪健康打卡 DTO
 * 用于猫咪管家（cat_keeper）新增体重/疫苗/互动记录
 */
public class CatHealthRecordDTO {

    private Integer catId;

    /** 记录类型：WEIGHT / VACCINE / INTERACTION */
    private String recordType;

    /** 记录值：体重如"4.6kg"、疫苗名如"猫三联"、互动类型如"梳毛" */
    private String recordValue;

    /** 备注：疫苗的 nextDue=yyyy-MM-dd、互动的 mood=happy|描述 */
    private String note;

    /** 记录日期，不传则默认当天 */
    private Date recordDate;

    /** 操作人 staffId（从前端传入或从 token 解析） */
    private Long staffId;

    public Integer getCatId() { return catId; }
    public void setCatId(Integer catId) { this.catId = catId; }

    public String getRecordType() { return recordType; }
    public void setRecordType(String recordType) { this.recordType = recordType; }

    public String getRecordValue() { return recordValue; }
    public void setRecordValue(String recordValue) { this.recordValue = recordValue; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public Date getRecordDate() { return recordDate; }
    public void setRecordDate(Date recordDate) { this.recordDate = recordDate; }

    public Long getStaffId() { return staffId; }
    public void setStaffId(Long staffId) { this.staffId = staffId; }
}
