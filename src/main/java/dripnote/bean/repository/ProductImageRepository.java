package dripnote.bean.repository;

import dripnote.bean.domain.ProductImage;
import dripnote.bean.enums.ImageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    // productIds에 있는 productId를 기반으로 지정한 ImageType에 포함되는 이미지 객체만 리스트 반환
    List<ProductImage> findByProduct_ProductIdInAndImageType(
            Collection<Long> productIds,
            ImageType imageType
    );

    List<ProductImage> findByProduct_ProductIdOrderBySortOrderAsc(Long productId);

    Optional<ProductImage> findByProduct_ProductIdAndImageType(Long productId, ImageType imageType);

    List<ProductImage> findByProduct_ProductIdAndImageTypeOrderBySortOrderAsc(Long productId, ImageType imageType);

    @Query("""
        select coalesce(max(bi.sortOrder), 0)
        from ProductImage bi
        where bi.product.productId = :productId
          and bi.imageType = dripnote.bean.enums.ImageType.SUB
    """)
    Integer findMaxSubSortOrder(Long productId);
}
