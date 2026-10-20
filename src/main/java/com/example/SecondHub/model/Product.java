package com.example.SecondHub.model;



import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Integer price;

    @Column(name = "product_condition")
    private String condition;

    private String description;

    // 商品分类，例如数码产品、游戏设备、学习用品
    private String category;

    /**
     * 商品发布时间
     * 新建商品时由 Hibernate 自动生成
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * 商品最近更新时间
     * 创建时和更新时由 Hibernate 自动维护
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // JPA 必须能够通过无参构造方法创建实体对象
    public Product() {
    }

    // 保留原来的构造方法，暂时兼容现有 Service
    public Product(
            Long id,
            String name,
            Integer price,
            String condition,
            String description
    ) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.condition = condition;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

}
