package baristation.bookmark.service;

import baristation.bean.payload.dto.ProductSummaryDTO;
import baristation.common.payload.response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface BookmarkService {
    /**
     * productId에 대해 현재 인증 사용자 기준으로 토글 동작:
     * - 이미 존재하면 삭제
     * - 없으면 생성
     */
    void toggleBookmark(Long productId, Long userId);

    PageResponse<ProductSummaryDTO> getBookmarks(Long userId, Pageable pageable);
}
