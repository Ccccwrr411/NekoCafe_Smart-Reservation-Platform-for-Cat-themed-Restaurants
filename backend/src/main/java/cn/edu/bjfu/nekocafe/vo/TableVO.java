package cn.edu.bjfu.nekocafe.vo;

/**
 * VO - 桌位列表响应体（对应接口 C-1）
 * entity Tables + TableStatus → 组合桌位基本信息与实时状态
 */
public class TableVO {
    private Integer id;         // tableId
    private String name;        // tableNo
    private String type;        // tableType
    private Integer capacity;
    private String status;      // available / booked / maintenance
    private String catType;     // catTheme（品种）
    private String catName;     // 关联猫咪名字（需联查 cat_profiles）
    private Integer price;      // 包间附加费（暂为 0）
    private String top;
    private String left;
    private String width;
    private String height;
    public String getTop() { return top; }
    public void setTop(String top) { this.top = top; }

    public String getLeft() { return left; }
    public void setLeft(String left) { this.left = left; }

    public String getWidth() { return width; }
    public void setWidth(String width) { this.width = width; }

    public String getHeight() { return height; }
    public void setHeight(String height) { this.height = height; }
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCatType() { return catType; }
    public void setCatType(String catType) { this.catType = catType; }
    public String getCatName() { return catName; }
    public void setCatName(String catName) { this.catName = catName; }
    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }
}
