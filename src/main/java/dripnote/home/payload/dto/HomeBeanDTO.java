package dripnote.home.payload.dto;

import dripnote.bean.domain.Bean;

import java.util.List;

public record HomeBeanDTO(
        String beanName,
        List<String> beanTasting,
        String beanImageLink,
        String beanLink
) {
    // Entity + 관련 데이터 → DTO 변환: 정적 팩토리 메서드 사용
    public static HomeBeanDTO of(Bean bean, List<String> tastings, String imageUrl) {
        return new HomeBeanDTO(
                bean.getNameKo(),
                tastings,
                imageUrl,
                "/beans/detail/" + bean.getBeanId()
        );
    }
}
