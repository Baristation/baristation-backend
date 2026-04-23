package dripnote.bean.service;

import dripnote.common.payload.response.PageResponse;
import dripnote.bean.payload.request.BeanSearchRequest;
import dripnote.bean.payload.dto.ProductListItemDTO; // (필요하다면 BeanDTO 등으로 이름 변경 권장)
import org.springframework.data.domain.Pageable;

public interface BeanService {
    /**
     * 조건에 맞는 원두 목록을 검색
     */
    PageResponse<ProductListItemDTO> searchBeans(BeanSearchRequest request, Pageable pageable);
    
    /**
     * 원두 단건 상세 조회
     */
    ProductListItemDTO getBeanDetail(Long beanId);
}