package dripnote.bean.service;

import dripnote.common.payload.response.PageResponse;
import dripnote.bean.payload.request.ProductSearchRequest;
import dripnote.bean.payload.dto.ProductDetailDTO;
import dripnote.bean.payload.dto.ProductSummaryDTO;
import org.springframework.data.domain.Pageable;

public interface BeanService {
    /**
     * 조건에 맞는 원두 목록을 검색
     */
    PageResponse<ProductSummaryDTO> searchProducts(ProductSearchRequest request, Pageable pageable);
    
    /**
     * 원두 단건 상세 조회
     */
    ProductDetailDTO getProductDetail(Long productId);
}