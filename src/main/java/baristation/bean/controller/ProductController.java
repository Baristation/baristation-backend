package baristation.bean.controller;

import baristation.bean.payload.dto.ProductDetailDTO;
import baristation.bean.payload.dto.ProductSummaryDTO;
import baristation.bean.payload.request.ProductSearchRequest;
import baristation.bean.service.BeanService;
import baristation.common.logging.TraceIdUtil;
import baristation.common.payload.response.ApiResponse;
import baristation.common.payload.response.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/products")
public class ProductController {
    private final BeanService beanService;

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<ProductSummaryDTO>>> searchProducts(
            @ModelAttribute ProductSearchRequest request,
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.info("[Product] searchProducts start. page={}, size={}, keyword={}, roastingType={}, sortBy={}, traceId={}",
                pageable.getPageNumber(), pageable.getPageSize(), request.keyword(), request.roastingType(), request.sortBy(), TraceIdUtil.getTraceId());
        PageResponse<ProductSummaryDTO> response = beanService.searchProducts(request, pageable);
        log.info("[Product] searchProducts done. contentSize={}, totalElements={}, traceId={}",
                response.content().size(), response.totalElements(), TraceIdUtil.getTraceId());
        return ApiResponse.ok(response);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductDetailDTO>> getProductDetail(@PathVariable Long productId) {
        log.info("[Product] getProductDetail start. productId={}, traceId={}", productId, TraceIdUtil.getTraceId());
        ProductDetailDTO response = beanService.getProductDetail(productId);
        log.info("[Product] getProductDetail done. productId={}, hasDetail={}, traceId={}",
                productId, response != null, TraceIdUtil.getTraceId());
        return ApiResponse.ok(response);
    }





}
