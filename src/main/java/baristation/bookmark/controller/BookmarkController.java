package baristation.bookmark.controller;

import baristation.bean.payload.dto.ProductSummaryDTO;
import baristation.bookmark.service.BookmarkService;
import baristation.common.logging.TraceIdUtil;
import baristation.common.payload.response.ApiResponse;
import baristation.common.payload.response.PageResponse;
import baristation.security.annotation.CurrentUserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
@Slf4j
public class BookmarkController {
    private final BookmarkService bookmarkService;
    // 북마크 토글(추가/삭제)
    @PostMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> toggleBookmark(
            @PathVariable Long productId,
            @CurrentUserId Long userId
    ) {
        bookmarkService.toggleBookmark(productId, userId);
        log.info("[Bookmark] toggle done. productId={}, userId={}, traceId={}",
                productId, userId, TraceIdUtil.getTraceId());
        return ApiResponse.ok(null);
    }
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<PageResponse<ProductSummaryDTO>>> getBookmarks(
            @CurrentUserId Long userId,
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        log.info("[Bookmark] getBookmarks start. userId={}, traceId={}",
                userId, TraceIdUtil.getTraceId());
        PageResponse<ProductSummaryDTO> response = bookmarkService.getBookmarks(userId, pageable);
        log.info("[Bookmark] getBookmarks done. userId={}, traceId={}",
                userId, TraceIdUtil.getTraceId());
        return ApiResponse.ok(response);
    }
}
