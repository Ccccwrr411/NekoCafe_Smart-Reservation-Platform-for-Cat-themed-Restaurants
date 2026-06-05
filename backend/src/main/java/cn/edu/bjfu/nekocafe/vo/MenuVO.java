package cn.edu.bjfu.nekocafe.vo;

import java.util.List;

/**
 * VO - 菜单响应体（对应接口 D-1）
 * 包含分类列表和菜品列表两部分
 */
public class MenuVO {

    private List<CategoryVO> categories;
    private List<MenuItemVO> items;

    public List<CategoryVO> getCategories() { return categories; }
    public void setCategories(List<CategoryVO> categories) { this.categories = categories; }
    public List<MenuItemVO> getItems() { return items; }
    public void setItems(List<MenuItemVO> items) { this.items = items; }

    /** 菜品分类 */
    public static class CategoryVO {
        private Integer id;
        private String name;
        private String icon;

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
    }

    /** 单个菜品 */
    public static class MenuItemVO {
        private Integer id;
        private Integer categoryId;
        private String name;
        private Integer price;
        private String imageUrl;
        private String desc;
        private Integer sales;
        private Double rating;
        private Boolean isHot;
        private Boolean isNew;

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public Integer getCategoryId() { return categoryId; }
        public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getPrice() { return price; }
        public void setPrice(Integer price) { this.price = price; }
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        public String getDesc() { return desc; }
        public void setDesc(String desc) { this.desc = desc; }
        public Integer getSales() { return sales; }
        public void setSales(Integer sales) { this.sales = sales; }
        public Double getRating() { return rating; }
        public void setRating(Double rating) { this.rating = rating; }
        public Boolean getIsHot() { return isHot; }
        public void setIsHot(Boolean isHot) { this.isHot = isHot; }
        public Boolean getIsNew() { return isNew; }
        public void setIsNew(Boolean isNew) { this.isNew = isNew; }
    }
}
