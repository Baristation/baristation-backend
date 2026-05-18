package baristation.bookmark.controller;

import baristation.bookmark.service.BookmarkService;
import baristation.common.logging.TraceIdUtil;
import baristation.common.payload.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
            @AuthenticationPrincipal UserDetails userDetails // 스프링이 토큰 까서 넣어줌
    ) {
        Long userId = Long.valueOf(userDetails.getUsername());
        log.info("[Bookmark] toggle start. productId={}, userId={}, traceId={}",
                productId, userId, TraceIdUtil.getTraceId());
        bookmarkService.toggleBookmark(productId, userId);
        log.info("[Bookmark] toggle done. productId={}, userId={}, traceId={}",
                productId, userId, TraceIdUtil.getTraceId());
        return ApiResponse.ok(null);
    }

}
