package dripnote.bean.repository;

import dripnote.bean.domain.ProductFlavorNote;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ProductFlavorNoteRepository extends JpaRepository<ProductFlavorNote, Long> {

    // productIds에 있는 상품을 조회해서 FlavorNote 반환
    // LazyInitializationException이 나지않도록 product와 flavorNote를 같이 로딩
    @EntityGraph(attributePaths = {"product", "flavorNote"})
    List<ProductFlavorNote> findByProduct_ProductIdIn(Collection<Long> productIds);

}
