package com.example.SecondHub.repository;

import com.example.SecondHub.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // 按商品名称搜索，并分页
    Page<Product> findByNameContainingIgnoreCase(
            String keyword,
            Pageable pageable
    );

    // 按商品分类筛选，并分页
    Page<Product> findByCategory(
            String category,
            Pageable pageable
    );

    // 同时按名称和分类筛选，并分页
    Page<Product> findByNameContainingIgnoreCaseAndCategory(
            String keyword,
            String category,
            Pageable pageable
    );

}
