package dripnote.home.controller;

import dripnote.common.payload.response.ApiResponse;
import dripnote.common.payload.response.PageResponse;
import dripnote.bean.payload.dto.BeanListItemDTO;
import dripnote.bean.payload.request.BeanSearchRequest;
import dripnote.home.service.HomeServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HomeController {
    private final HomeServiceImpl homeService;

    @GetMapping("/api/beans")
    public ResponseEntity<ApiResponse<PageResponse<BeanListItemDTO>>> searchBeans(
            @ModelAttribute BeanSearchRequest condition, // 커스텀 필터 조건
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable // 페이징 및 정렬 조건
    ) {
        return ResponseEntity.ok(ApiResponse.ok(homeService.getHome(condition, pageable)));
    }
}
