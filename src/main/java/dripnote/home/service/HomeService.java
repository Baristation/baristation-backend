package dripnote.home.service;

import dripnote.home.payload.dto.HomeBeanDTO;
import dripnote.home.payload.request.BeanSearchRequest;
import dripnote.home.payload.response.HomeResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface HomeService {
    /**
     * 메인 페이지에 필요한 향미(Tastings)와 원두(Beans) 통합 정보 조회
     */
    HomeResponse getHome(BeanSearchRequest beanSearchRequest,
                         Pageable pageable);

    List<HomeBeanDTO> getBeans();
}
