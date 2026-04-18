package dripnote.home.service;

import dripnote.bean.payload.dto.BeanListItemDTO;
import dripnote.bean.payload.request.BeanSearchRequest;
import dripnote.common.payload.response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface HomeService {
    /**
     * 원두 목록을 검색 조건에 따라 페이지네이션하여 반환
     */
    PageResponse<BeanListItemDTO> getHome(BeanSearchRequest request, Pageable pageable);
}
