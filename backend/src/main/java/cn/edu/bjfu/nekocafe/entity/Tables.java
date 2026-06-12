package cn.edu.bjfu.nekocafe.entity;

public class Tables {
    private Integer tableId;

    private Integer storeId;

    private String tableNo;

    private Integer capacity;

    private String tableType;

    private String catTheme;

    private Boolean isActive;

    public Integer getTableId() {
        return tableId;
    }

    public void setTableId(Integer tableId) {
        this.tableId = tableId;
    }

    public Integer getStoreId() {
        return storeId;
    }

    public void setStoreId(Integer storeId) {
        this.storeId = storeId;
    }

    public String getTableNo() {
        return tableNo;
    }

    public void setTableNo(String tableNo) {
        this.tableNo = tableNo;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public String getTableType() {
        return tableType;
    }

    public void setTableType(String tableType) {
        this.tableType = tableType;
    }

    public String getCatTheme() {
        return catTheme;
    }

    public void setCatTheme(String catTheme) {
        this.catTheme = catTheme;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}