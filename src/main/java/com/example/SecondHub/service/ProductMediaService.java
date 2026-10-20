package com.example.SecondHub.service;


import com.example.SecondHub.model.Product;
import com.example.SecondHub.model.ProductMedia;
import com.example.SecondHub.repository.ProductMediaRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductMediaService {

    private static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;
    private static final long MAX_VIDEO_SIZE = 100L * 1024 * 1024;

    private static final int MAX_IMAGES = 9;

    private final ProductMediaRepository mediaRepository;

    @Value("${app.media.storage-dir}")
    private String storageDir;

    private Path storagePath;

    public ProductMediaService(
            ProductMediaRepository mediaRepository
    ) {
        this.mediaRepository = mediaRepository;
    }

    // 应用启动后创建媒体存储目录
    @PostConstruct
    public void initializeStorage() throws IOException {
        storagePath = Paths.get(storageDir)
                .toAbsolutePath()
                .normalize();

        Files.createDirectories(storagePath);
    }

    // 上传图片或视频
    public ProductMedia upload(
            Product product,
            MultipartFile file
    ) throws IOException {

        if (product == null || product.getId() == null) {
            throw new IllegalArgumentException("商品不存在或尚未保存");
        }

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请选择要上传的文件");
        }

        String originalName = file.getOriginalFilename();

        if (originalName == null || originalName.isBlank()) {
            throw new IllegalArgumentException("文件名无效");
        }

        // 只获取扩展名，不使用原始文件名作为保存路径
        String fileName = Paths.get(originalName)
                .getFileName()
                .toString();

        int dotIndex = fileName.lastIndexOf('.');

        if (dotIndex <= 0 || dotIndex == fileName.length() - 1) {
            throw new IllegalArgumentException("文件必须具有有效扩展名");
        }

        String extension = fileName
                .substring(dotIndex + 1)
                .toLowerCase(Locale.ROOT);

        ProductMedia.MediaType mediaType;
        String contentType;
        long maxSize;

        switch (extension) {
            case "jpg", "jpeg" -> {
                mediaType = ProductMedia.MediaType.IMAGE;
                contentType = "image/jpeg";
                maxSize = MAX_IMAGE_SIZE;
            }
            case "png" -> {
                mediaType = ProductMedia.MediaType.IMAGE;
                contentType = "image/png";
                maxSize = MAX_IMAGE_SIZE;
            }
            case "webp" -> {
                mediaType = ProductMedia.MediaType.IMAGE;
                contentType = "image/webp";
                maxSize = MAX_IMAGE_SIZE;
            }
            case "mp4" -> {
                mediaType = ProductMedia.MediaType.VIDEO;
                contentType = "video/mp4";
                maxSize = MAX_VIDEO_SIZE;
            }
            case "webm" -> {
                mediaType = ProductMedia.MediaType.VIDEO;
                contentType = "video/webm";
                maxSize = MAX_VIDEO_SIZE;
            }
            default -> throw new IllegalArgumentException(
                    "仅支持 JPG、PNG、WebP 图片及 MP4、WebM 视频"
            );
        }

        // 校验文件大小
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException(
                    mediaType == ProductMedia.MediaType.IMAGE
                            ? "单张图片不能超过 5 MB"
                            : "单个视频不能超过 100 MB"
            );
        }

        Long productId = product.getId();

        // 校验商品媒体数量
        long count = mediaRepository.countByProduct_IdAndMediaType(
                productId,
                mediaType
        );

        if (mediaType == ProductMedia.MediaType.IMAGE
                && count >= MAX_IMAGES) {
            throw new IllegalArgumentException("每件商品最多上传 9 张图片");
        }

        if (mediaType == ProductMedia.MediaType.VIDEO
                && mediaRepository.existsByProduct_IdAndMediaType(
                productId,
                ProductMedia.MediaType.VIDEO
        )) {
            throw new IllegalArgumentException("每件商品最多上传 1 个视频");
        }

        // 检查文件实际签名，不能只相信扩展名
        validateFileSignature(file, extension);

        // 使用随机文件名，避免重名覆盖和路径穿越
        String storedFileName = UUID.randomUUID()
                + "."
                + extension;

        Path destination = storagePath.resolve(storedFileName)
                .normalize();

        if (!destination.startsWith(storagePath)) {
            throw new IllegalArgumentException("非法文件路径");
        }

        // 先保存文件
        Files.copy(file.getInputStream(), destination);

        try {
            // 再保存数据库记录
            ProductMedia media = new ProductMedia();

            media.setProduct(product);
            media.setMediaType(mediaType);
            media.setOriginalFileName(fileName);
            media.setStoredFileName(storedFileName);
            media.setContentType(contentType);
            media.setFileSize(file.getSize());

            return mediaRepository.save(media);

        } catch (RuntimeException exception) {
            // 数据库保存失败时，尽量清理已保存的文件
            try {
                Files.deleteIfExists(destination);
            } catch (IOException cleanupException) {
                exception.addSuppressed(cleanupException);
            }

            throw exception;
        }
    }


    /**
     * 根据存储文件名查询媒体记录
     */
    public Optional<ProductMedia> findByStoredFileName(
            String storedFileName
    ) {
        // 防止通过路径穿越读取其他文件
        if (storedFileName == null
                || storedFileName.isBlank()
                || !Paths.get(storedFileName)
                .getFileName()
                .toString()
                .equals(storedFileName)) {
            return Optional.empty();
        }

        return mediaRepository.findByStoredFileName(storedFileName);
    }

    /**
     * 从磁盘读取已经登记在数据库中的媒体文件
     */
    public Resource loadAsResource(ProductMedia media) throws IOException {
        if (media == null
                || media.getStoredFileName() == null
                || media.getStoredFileName().isBlank()) {
            throw new IOException("媒体记录无效");
        }

        Path file = storagePath
                .resolve(media.getStoredFileName())
                .normalize();

        // 确保目标文件仍然位于媒体存储目录内
        if (!file.startsWith(storagePath)) {
            throw new IOException("非法的媒体文件路径");
        }

        if (!Files.isRegularFile(file)) {
            throw new IOException("媒体文件不存在");
        }

        Resource resource = new UrlResource(file.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new IOException("媒体文件无法读取");
        }

        return resource;
    }


    // 检查常见图片、视频格式的文件签名
    private void validateFileSignature(
            MultipartFile file,
            String extension
    ) throws IOException {

        byte[] header;

        try (InputStream input = file.getInputStream()) {
            header = input.readNBytes(12);
        }

        boolean valid = switch (extension) {
            case "jpg", "jpeg" -> header.length >= 3
                    && unsigned(header[0]) == 0xFF
                    && unsigned(header[1]) == 0xD8
                    && unsigned(header[2]) == 0xFF;

            case "png" -> header.length >= 8
                    && unsigned(header[0]) == 0x89
                    && header[1] == 0x50
                    && header[2] == 0x4E
                    && header[3] == 0x47
                    && header[4] == 0x0D
                    && header[5] == 0x0A
                    && header[6] == 0x1A
                    && header[7] == 0x0A;

            case "webp" -> header.length >= 12
                    && ascii(header, 0, 4).equals("RIFF")
                    && ascii(header, 8, 4).equals("WEBP");

            case "mp4" -> header.length >= 8
                    && ascii(header, 4, 4).equals("ftyp");

            case "webm" -> header.length >= 4
                    && unsigned(header[0]) == 0x1A
                    && unsigned(header[1]) == 0x45
                    && unsigned(header[2]) == 0xDF
                    && unsigned(header[3]) == 0xA3;

            default -> false;
        };

        if (!valid) {
            throw new IllegalArgumentException(
                    "文件实际格式与扩展名不匹配，或文件内容无效"
            );
        }
    }

    private int unsigned(byte value) {
        return value & 0xFF;
    }

    private String ascii(byte[] bytes, int offset, int length) {
        return new String(
                bytes,
                offset,
                length,
                java.nio.charset.StandardCharsets.US_ASCII
        );
    }

    /**
     * 删除指定商品关联的媒体
     */
    public boolean delete(Long productId, Long mediaId)
            throws IOException {

        // 1. 查询媒体，并确认它属于指定商品
        ProductMedia media = mediaRepository
                .findByIdAndProduct_Id(mediaId, productId)
                .orElse(null);

        if (media == null) {
            return false;
        }

        // 2. 构造实际文件路径
        Path file = storagePath
                .resolve(media.getStoredFileName())
                .normalize();

        if (!file.startsWith(storagePath)) {
            throw new IOException("非法的媒体文件路径");
        }

        // 3. 先删除数据库记录
        mediaRepository.delete(media);

        // 4. 再清理磁盘文件
        try {
            Files.deleteIfExists(file);
        } catch (IOException exception) {
            System.err.println(
                    "[ProductMediaService] 数据库记录已删除，"
                            + "但磁盘文件清理失败："
                            + media.getStoredFileName()
            );

            throw exception;
        }

        return true;
    }


    /**
     * 批量上传商品图片和视频。
     * 如果其中一个文件失败，尝试清理本次已经上传的媒体。
     */
    public void uploadAll(
            Product product,
            java.util.List<MultipartFile> images,
            MultipartFile video
    ) throws IOException {

        java.util.List<ProductMedia> uploaded = new java.util.ArrayList<>();

        // 先检查本次图片数量，避免上传到一半才发现超限
        java.util.List<MultipartFile> validImages = images == null
                ? new java.util.ArrayList<>()
                : images.stream()
                .filter(file -> file != null && !file.isEmpty())
                .toList();

        if (validImages.size() > 9) {
            throw new IllegalArgumentException("每件商品最多上传 9 张图片");
        }

        try {
            // 先上传图片
            for (MultipartFile image : validImages) {
                uploaded.add(upload(product, image));
            }

            // 再上传视频（允许不选视频）
            if (video != null && !video.isEmpty()) {
                uploaded.add(upload(product, video));
            }

        } catch (IOException | RuntimeException exception) {
            // 本次上传失败，尝试删除已经成功上传的媒体
            for (ProductMedia media : uploaded) {
                try {
                    delete(product.getId(), media.getId());
                } catch (Exception cleanupException) {
                    exception.addSuppressed(cleanupException);
                    System.err.println(
                            "[ProductMediaService] 清理失败，媒体 ID："
                                    + media.getId()
                    );
                }
            }

            throw exception;
        }
    }

}
