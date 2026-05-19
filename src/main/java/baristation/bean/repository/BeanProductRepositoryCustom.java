package baristation.bean.repository;

import baristation.bean.domain.BeanProduct;
import baristation.bean.payload.request.ProductSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BeanProductRepositoryCustom {
    // 동적 검색과 페이징을 DB에서 끝내고 Page 객체로 반환
    Page<BeanProduct> searchProductsWithFilters(ProductSearchRequest request, Pageable pageable);
    Page<BeanProduct> searchProductsWithUserId(Pageable pageable, Long userId);
    BeanProduct findByProductId(Long productId);
}