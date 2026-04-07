package dripnote.bean.repository;

import dripnote.bean.domain.BeanImage;
import dripnote.bean.domain.BeanTastingNote;
import dripnote.bean.enums.ImageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BeanImagesRepository extends JpaRepository<BeanImage, Long> {

    // beanIds에 있는 beanId를 기반으로 지정한 ImageType에 포함되는 이미지 객체만 리스트 반환
    List<BeanImage> findByBean_BeanIdInAndImageType(Collection<Long> beanIds, ImageType imageType);

    List<BeanImage> findByBean_BeanIdOrderBySortOrderAsc(Long beanId);

    Optional<BeanImage> findByBean_BeanIdAndImageType(Long beanId, ImageType imageType);

    List<BeanImage> findByBean_BeanIdAndImageTypeOrderBySortOrderAsc(Long beanId, ImageType imageType);

    @Query("""
        select coalesce(max(bi.sortOrder), 0)
        from BeanImage bi
        where bi.bean.beanId = :beanId
          and bi.imageType = dripnote.bean.enums.ImageType.SUB
    """)
    Integer findMaxSubSortOrder(Long beanId);
}
