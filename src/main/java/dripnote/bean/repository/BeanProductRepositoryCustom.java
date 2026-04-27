package dripnote.bean.repository;

import dripnote.bean.domain.BeanProduct;
import dripnote.bean.payload.request.ProductSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BeanProductRepositoryCustom {
    // 동적 검색과 페이징을 DB에서 끝내고 Page 객체로 반환
    Page<BeanProduct> searchBeansWithFilters(ProductSearchRequest request, Pageable pageable);
    BeanProduct findByProductId(Long productId);
}