package baristation.bookmark;

import baristation.bookmark.service.BookmarkService;
import baristation.common.payload.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {
    private final BookmarkService bookmarkService;

    // 북마크 토글(추가/삭제)
    @PostMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> toggleBookmark(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserDetails userDetails // 스프링이 토큰 까서 넣어줌
    ) {
        Long userId = Long.valueOf(userDetails.getUsername());
        bookmarkService.toggleBookmark(productId, userId);
        return ApiResponse.ok(null);
    }

}
