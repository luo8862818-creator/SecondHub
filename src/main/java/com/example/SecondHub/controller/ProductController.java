package com.example.SecondHub.controller;

import com.example.SecondHub.dto.ProductCreateRequest;
import com.example.SecondHub.model.Product;
import com.example.SecondHub.model.ProductMedia;
import com.example.SecondHub.repository.ProductMediaRepository;
import com.example.SecondHub.service.ProductMediaService;
import com.example.SecondHub.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {
    private final ProductService productService;
    private final ProductMediaService productMediaService;
    private final ProductMediaRepository productMediaRepository;

    public ProductController(ProductService productService, ProductMediaService productMediaService, ProductMediaRepository productMediaRepository) {
        this.productService = productService;
        this.productMediaService = productMediaService;
        this.productMediaRepository = productMediaRepository;
    }

    @GetMapping("/publish")
    public String publish(Model model) {
        model.addAttribute(
                "productCreateRequest",
                new ProductCreateRequest()
        );
        System.out.println("[ProductController] 打开商品发布页面");
        return "product/publish";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        System.out.println("[ProductController] 查询商品 ID：" + id);

        Product product = productService.findById(id)
                .orElse(null);

        if (product == null) {
            return "product/not-found";
        }

        model.addAttribute("product", product);
        List<ProductMedia> mediaList =
                productMediaRepository.findByProduct_IdOrderBySortOrderAscIdAsc(id);

        model.addAttribute("mediaList", mediaList);

        model.addAttribute(
                "imageList",
                mediaList.stream()
                        .filter(media ->
                                media.getMediaType() == ProductMedia.MediaType.IMAGE)
                        .toList()
        );

        model.addAttribute(
                "videoList",
                mediaList.stream()
                        .filter(media ->
                                media.getMediaType() == ProductMedia.MediaType.VIDEO)
                        .toList()
        );

        return "product/detail";
    }


    @PostMapping
    public String create(
            @Valid @ModelAttribute("productCreateRequest")
            ProductCreateRequest request,
            BindingResult bindingResult,
            @RequestParam(value = "images", required = false)
            List<MultipartFile> images,
            @RequestParam(value = "video", required = false)
            MultipartFile video,
            RedirectAttributes redirectAttributes
    ) {
        System.out.println("[ProductController] 收到商品发布请求");

        // 1. 检查商品表单
        if (bindingResult.hasErrors()) {
            bindingResult.getFieldErrors().forEach(error ->
                    System.out.println(
                            "[校验错误] 字段：" + error.getField()
                                    + "，原因：" + error.getDefaultMessage()
                    )
            );

            return "product/publish";
        }

        // 2. 先保存商品，取得数据库生成的 ID
        Product product = productService.publish(
                request.getName().trim(),
                request.getPrice(),
                request.getCondition(),
                request.getDescription().trim(),
                request.getCategory()
        );

        System.out.println(
                "[ProductController] 商品已保存，ID：" + product.getId()
        );

        try {
            // 3. 上传图片和视频
            productMediaService.uploadAll(product, images, video);

            System.out.println(
                    "[ProductController] 商品及媒体上传成功，ID："
                            + product.getId()
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "商品发布成功"
            );

            // 4. 跳转到商品详情页
            return "redirect:/products/" + product.getId();

        } catch (Exception exception) {
            System.err.println(
                    "[ProductController] 媒体上传失败，商品 ID："
                            + product.getId()
            );
            exception.printStackTrace();

            // 上传失败时，尝试删除本次创建的商品
            try {
                productService.deleteById(product.getId());
            } catch (Exception cleanupException) {
                exception.addSuppressed(cleanupException);
                System.err.println(
                        "[ProductController] 商品清理失败，需检查数据库："
                                + product.getId()
                );
            }

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "商品发布失败：" + exception.getMessage()
            );

            return "redirect:/products/publish";
        }
    }


    @GetMapping("/{id}/edit")
    public String editPage(@PathVariable Long id, Model model) {
        Product product = productService.findById(id)
                .orElse(null);

        if (product == null) {
            return "product/not-found";
        }

        ProductCreateRequest request = new ProductCreateRequest();
        request.setName(product.getName());
        request.setPrice(product.getPrice());
        request.setCondition(product.getCondition());
        request.setDescription(product.getDescription());

        model.addAttribute("product", product);
        model.addAttribute("productCreateRequest", request);

        return "product/edit";
    }

    @PostMapping("/{id}/edit")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("productCreateRequest")
            ProductCreateRequest request,
            BindingResult bindingResult,
            Model model
    ) {
        System.out.println("[ProductController] 收到商品修改请求，ID：" + id);

        // 1. 查询原商品，确认它存在
        Product existing = productService.findById(id).orElse(null);

        if (existing == null) {
            return "product/not-found";
        }

        // 2. 校验失败，返回编辑页面
        if (bindingResult.hasErrors()) {
            model.addAttribute("product", existing);
            return "product/edit";
        }

        // 3. 更新商品
        Product updated = productService.update(
                id,
                request.getName().trim(),
                request.getPrice(),
                request.getCondition(),
                request.getDescription().trim(),
                request.getCategory()
        ).orElse(null);

        if (updated == null) {
            return "product/not-found";
        }

        System.out.println("[ProductController] 商品修改成功，ID：" + updated.getId());

        // 4. 返回当前商品详情页
        return "redirect:/products/" + updated.getId();
    }

    @PostMapping("/{id}/delete")
    public String delete(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {
        System.out.println("[ProductController] 收到删除请求，商品 ID：" + id);

        boolean deleted = productService.deleteById(id);

        if (!deleted) {
            System.out.println("[ProductController] 商品不存在，ID：" + id);
            return "product/not-found";
        }

        System.out.println("[ProductController] 删除成功，商品 ID：" + id);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "商品已删除"
        );

        return "redirect:/";
    }
}
