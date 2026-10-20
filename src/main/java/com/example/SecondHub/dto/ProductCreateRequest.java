package com.example.SecondHub.dto;



import jakarta.validation.constraints.*;

public class ProductCreateRequest {

    @NotBlank(message = "商品名称不能为空")
    @Size(max = 100, message = "商品名称不能超过100个字符")
    private String name;

    @NotNull(message = "请输入商品价格")
    @Positive(message = "商品价格必须大于0")
    private Integer price;

    @NotBlank(message = "请选择商品成色")
    @Pattern(
            regexp = "全新|九成新|八成新|七成新及以下",
            message = "商品成色不合法"
    )
    private String condition;


    @NotBlank(message = "请输入商品描述")
    @Size(max = 2000, message = "商品描述不能超过2000个字符")
    private String description;

    private String category;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
