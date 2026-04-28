package dripnote.home.service;

import dripnote.home.payload.response.HomeResponse;

public interface HomeService {

    /**
     * 메인 페이지에 필요한 향미 바로가기와 추천 상품 정보를 조회합니다.
     */
    HomeResponse getHome();
}
