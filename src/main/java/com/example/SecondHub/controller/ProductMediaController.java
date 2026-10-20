package com.example.SecondHub.controller;



import com.example.SecondHub.model.Product;
import com.example.SecondHub.model.ProductMedia;
import com.example.SecondHub.service.ProductMediaService;
import com.example.SecondHub.service.ProductService;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.io.IOException;

@Controller
public class ProductMediaController {

    private final ProductMediaService productMediaService;
    private final ProductService productService;

    public ProductMediaController(
            ProductMediaService productMediaService,
            ProductService productService
    ) {
        this.productMediaService = productMediaService;
        this.productService = productService;
    }

    /**
     * 上传商品图片或视频
     */
    @PostMapping("/products/{id}/media")
    public String upload(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes
    ) {
        // 1. 查询商品，确认商品存在
        Product product = productService.findById(id)
                .orElse(null);

        if (product == null) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "商品不存在，无法上传文件"
            );

            return "redirect:/";
        }

        try {
            // 2. 调用媒体服务完成文件校验和保存
            ProductMedia media = productMediaService.upload(
                    product,
                    file
            );

            System.out.println(
                    "[ProductMediaController] 上传成功，商品 ID："
                            + id
                            + "，媒体 ID："
                            + media.getId()
                            + "，类型："
                            + media.getMediaType()
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    media.getMediaType() == ProductMedia.MediaType.IMAGE
                            ? "图片上传成功"
                            : "视频上传成功"
            );

        } catch (IllegalArgumentException exception) {
            // 3. 处理文件类型、大小和数量等校验错误
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );

        } catch (Exception exception) {
            // 4. 记录服务器端异常
            System.err.println(
                    "[ProductMediaController] 上传失败，商品 ID：" + id
            );
            exception.printStackTrace();

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "上传失败，请检查服务器日志"
            );
        }

        // 5. 返回商品详情页
        return "redirect:/products/" + id;
    }


    /**
     * 访问商品图片或视频
     */
    @GetMapping("/media/{storedFileName:.+}")
    public ResponseEntity<Resource> viewMedia(
            @PathVariable String storedFileName
    ) {
        // 1. 查询数据库中的媒体记录
        ProductMedia media = productMediaService
                .findByStoredFileName(storedFileName)
                .orElse(null);

        if (media == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            // 2. 读取磁盘文件
            Resource resource = productMediaService.loadAsResource(media);

            // 3. 返回正确的媒体类型
            MediaType contentType = MediaType.parseMediaType(
                    media.getContentType()
            );

            return ResponseEntity.ok()
                    .contentType(contentType)
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + storedFileName + "\""
                    )
                    .body(resource);

        } catch (IOException | IllegalArgumentException exception) {
            System.err.println(
                    "[ProductMediaController] 读取媒体失败："
                            + storedFileName
                            + "，原因："
                            + exception.getMessage()
            );

            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 删除商品图片或视频
     */
    @PostMapping("/products/{productId}/media/{mediaId}/delete")
    public String deleteMedia(
            @PathVariable Long productId,
            @PathVariable Long mediaId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            boolean deleted = productMediaService.delete(
                    productId,
                    mediaId
            );

            if (!deleted) {
                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "媒体不存在，或不属于该商品"
                );
            } else {
                redirectAttributes.addFlashAttribute(
                        "successMessage",
                        "媒体已删除"
                );
            }

        } catch (Exception exception) {
            System.err.println(
                    "[ProductMediaController] 删除媒体失败，媒体 ID："
                            + mediaId
            );

            exception.printStackTrace();

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "媒体删除过程中出现异常，请检查服务器日志"
            );
        }

        return "redirect:/products/" + productId;
    }

}
