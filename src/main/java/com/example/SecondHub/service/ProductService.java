package com.example.SecondHub.service;


import com.example.SecondHub.model.Product;
import com.example.SecondHub.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }


    // 根据 ID 查询商品
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    // 发布商品
    public Product publish(
            String name,
            Integer price,
            String condition,
            String description,
            String category
    ) {
        Product product = new Product();

        product.setName(name);
        product.setPrice(price);
        product.setCondition(condition);
        product.setDescription(description);
        product.setCategory(category);

        return productRepository.save(product);
    }

    // 更新商品
    public Optional<Product> update(
            Long id,
            String name,
            Integer price,
            String condition,
            String description,
            String category
    ) {
        Optional<Product> result = productRepository.findById(id);

        if (result.isEmpty()) {
            return Optional.empty();
        }

        Product existing = result.get();
        existing.setName(name);
        existing.setPrice(price);
        existing.setCondition(condition);
        existing.setDescription(description);
        existing.setCategory(category);

        return Optional.of(productRepository.save(existing));
    }

    // 删除商品
    public boolean deleteById(Long id) {
        if (!productRepository.existsById(id)) {
            return false;
        }

        productRepository.deleteById(id);
        return true;
    }



    /**
     * 分页查询最新商品
     */
    public Page<Product> listLatestProducts(int page, int size) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "updatedAt")
                        .and(Sort.by(Sort.Direction.DESC, "id"))
        );

        return productRepository.findAll(pageable);
    }

    /**
     * 根据商品名称和分类进行分页查询
     */
    public Page<Product> search(
            String keyword,
            String category,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "id")
        );

        boolean hasKeyword = keyword != null && !keyword.isBlank();
        boolean hasCategory = category != null && !category.isBlank();

        if (hasKeyword && hasCategory) {
            return productRepository
                    .findByNameContainingIgnoreCaseAndCategory(
                            keyword.trim(),
                            category.trim(),
                            pageable
                    );
        }

        if (hasKeyword) {
            return productRepository
                    .findByNameContainingIgnoreCase(
                            keyword.trim(),
                            pageable
                    );
        }

        if (hasCategory) {
            return productRepository.findByCategory(
                    category.trim(),
                    pageable
            );
        }

        return productRepository.findAll(pageable);
    }


}
