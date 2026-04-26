package dripnote.bean.controller;

import dripnote.bean.payload.dto.BeanDetailDTO;
import dripnote.bean.payload.dto.BeanSummaryDTO;
import dripnote.bean.payload.request.BeanSearchRequest;
import dripnote.bean.service.BeanService;
import dripnote.common.payload.response.ApiResponse;
import dripnote.common.payload.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/beans")
public class BeanController {
    private final BeanService beanService;

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<BeanSummaryDTO>>> searchBeans(
            @ModelAttribute BeanSearchRequest request,
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.ok(beanService.searchBeans(request, pageable)));
    }

    @GetMapping("/{beanId}")
    public ResponseEntity<ApiResponse<BeanDetailDTO>> getBeanDetail(@PathVariable Long beanId) {
        return ResponseEntity.ok(ApiResponse.ok(beanService.getBeanDetail(beanId)));
    }

}
