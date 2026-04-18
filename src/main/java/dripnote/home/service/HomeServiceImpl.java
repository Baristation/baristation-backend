package dripnote.home.service;

import dripnote.bean.payload.dto.BeanListItemDTO;
import dripnote.bean.payload.request.BeanSearchRequest;
import dripnote.bean.service.BeanService;
import dripnote.common.payload.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Home 기능 서비스 구현
 * Bean 서비스를 활용하여 원두 목록 및 추천 리스트를 제공
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class HomeServiceImpl implements HomeService {

    @Qualifier("beanServiceImpl")
    private final BeanService beanService;

    /**
     * 원두 목록을 검색 조건에 따라 페이지네이션하여 반환
     * 
     * @param request 검색 필터 조건
     * @param pageable 페이지네이션 정보
     * @return 페이지네이션된 원두 목록
     */
    @Override
    public PageResponse<BeanListItemDTO> getHome(BeanSearchRequest request, Pageable pageable) {
        return beanService.searchBeans(request, pageable);
    }
}

