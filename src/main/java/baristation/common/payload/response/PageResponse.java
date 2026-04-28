package baristation.common.payload.response;

import org.springframework.data.domain.Page;
import java.util.List;

public record PageResponse<T>(
        List<T> content,        // 실제 데이터 목록
        int totalPages,         // 전체 페이지 수
        long totalElements,     // 전체 데이터 건수
        int currentPage,        // 현재 페이지 번호 (0부터 시작)
        int size,               // 한 페이지당 데이터 개수
        boolean hasNext,        // 다음 페이지 존재 여부
        boolean hasPrevious     // 이전 페이지 존재 여부
) {
    /**
     * Spring Data JPA의 Page 객체를 받아서 DTO로 변환하는 팩토리 메서드
     */
    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.getNumber(),
                page.getSize(),
                page.hasNext(),
                page.hasPrevious()
        );
    }
}