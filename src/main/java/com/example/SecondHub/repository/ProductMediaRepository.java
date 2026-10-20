package com.example.SecondHub.repository;



import com.example.SecondHub.model.ProductMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductMediaRepository
        extends JpaRepository<ProductMedia, Long> {

    // 查询某个商品的全部媒体，按展示顺序排列
    List<ProductMedia> findByProduct_IdOrderBySortOrderAscIdAsc(
            Long productId
    );

    // 查询某个商品的全部图片
    List<ProductMedia> findByProduct_IdAndMediaTypeOrderBySortOrderAsc(
            Long productId,
            ProductMedia.MediaType mediaType
    );

    // 查询某个商品的视频
    List<ProductMedia> findByProduct_IdAndMediaType(
            Long productId,
            ProductMedia.MediaType mediaType
    );

    // 查询某个商品是否已有指定类型的媒体
    boolean existsByProduct_IdAndMediaType(
            Long productId,
            ProductMedia.MediaType mediaType
    );

    // 查询某个商品关联的媒体数量
    long countByProduct_Id(Long productId);

    // 删除某个商品关联的全部媒体记录
    void deleteByProduct_Id(Long productId);


    // 查询商品指定类型的媒体数量
    long countByProduct_IdAndMediaType(
            Long productId,
            ProductMedia.MediaType mediaType
    );

    // 根据服务器保存的唯一文件名查询媒体记录
    Optional<ProductMedia> findByStoredFileName(String storedFileName);

    // 只查询属于指定商品的媒体记录
    Optional<ProductMedia> findByIdAndProduct_Id(
            Long id,
            Long productId
    );
}
