package baristation.bookmark.service;

public interface BookmarkService {
    /**
     * productId에 대해 현재 인증 사용자 기준으로 토글 동작:
     * - 이미 존재하면 삭제
     * - 없으면 생성
     */
    void toggleBookmark(Long productId, Long userId);

}
