package dripnote.bean.service;

import dripnote.common.payload.response.PageResponse;
import dripnote.bean.payload.request.BeanSearchRequest;
import dripnote.bean.payload.dto.BeanDetailDTO;
import dripnote.bean.payload.dto.BeanSummaryDTO;
import org.springframework.data.domain.Pageable;

public interface BeanService {
    /**
     * 조건에 맞는 원두 목록을 검색
     */
    PageResponse<BeanSummaryDTO> searchBeans(BeanSearchRequest request, Pageable pageable);
    
    /**
     * 원두 단건 상세 조회
     */
    BeanDetailDTO getBeanDetail(Long beanId);
}