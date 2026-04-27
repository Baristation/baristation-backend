package dripnote.bean.controller;

import dripnote.bean.payload.dto.ProductDetailDTO;
import dripnote.bean.payload.dto.ProductSummaryDTO;
import dripnote.bean.payload.request.ProductSearchRequest;
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
@RequestMapping("/api/products")
public class ProductController {
    private final BeanService beanService;

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<ProductSummaryDTO>>> searchProducts(
            @ModelAttribute ProductSearchRequest request,
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.ok(beanService.searchProducts(request, pageable)));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductDetailDTO>> getProductDetail(@PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.ok(beanService.getProductDetail(productId)));
    }

}
