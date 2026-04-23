package dripnote.bean.repository;

import dripnote.bean.domain.ProductImage;
import dripnote.bean.enums.ImageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    // beanIds에 있는 beanId를 기반으로 지정한 ImageType에 포함되는 이미지 객체만 리스트 반환
    List<ProductImage> findByBean_BeanIdInAndImageType(Collection<Long> beanIds, ImageType imageType);

    List<ProductImage> findByBean_BeanIdOrderBySortOrderAsc(Long beanId);

    Optional<ProductImage> findByBean_BeanIdAndImageType(Long beanId, ImageType imageType);

    List<ProductImage> findByBean_BeanIdAndImageTypeOrderBySortOrderAsc(Long beanId, ImageType imageType);

    @Query("""
        select coalesce(max(bi.sortOrder), 0)
        from ProductImage bi
        where bi.product.productId = :productId
          and bi.imageType = dripnote.bean.enums.ImageType.SUB
    """)
    Integer findMaxSubSortOrder(Long beanId);
}
