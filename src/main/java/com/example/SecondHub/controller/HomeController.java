package com.example.SecondHub.controller;

import com.example.SecondHub.model.Product;
import com.example.SecondHub.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class HomeController {

    private final ProductService productService;

    public HomeController(ProductService productService) {
        this.productService = productService;
    }


    @GetMapping("/")
    public String index(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page, Model model) {
        // 每页显示 9 件商品
        int size = 9;

        // 防止负数页码
        if (page < 0) {
            page = 0;
        }

        boolean hasKeyword = keyword != null && !keyword.isBlank();
        boolean hasCategory = category != null && !category.isBlank();

        Page<Product> productPage;

        if (!hasKeyword && !hasCategory) {
            productPage = productService.listLatestProducts(page, size);
        } else {
            productPage = productService.search(keyword, category, page, size);
        }


        // 如果请求的页码超出范围，自动调整到最后一页
        if (productPage.getTotalPages() > 0 && page >= productPage.getTotalPages()) {

            page = productPage.getTotalPages() - 1;

            if (!hasKeyword && !hasCategory) {
                productPage = productService.listLatestProducts(page, size);
            } else {
                productPage = productService.search(keyword, category, page, size);
            }
        }


        model.addAttribute("products", productPage.getContent());
        model.addAttribute("productPage", productPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("category", category);

        return "index";
    }


}