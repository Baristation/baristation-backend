package dripnote.bean.payload.dto;

import dripnote.bean.domain.Bean;
import dripnote.bean.enums.AromaType;

import java.util.List;

/**
 * 원두 목록 페이지 및 추천 리스트에서 사용되는 원두 DTO
 * Bean 엔티티와 관련 데이터(맛, 이미지 등)를 조합한 응답 DTO
 */
public record BeanListItemDTO(
        Long beanId,
        String beanNameKo,
        String beanNameEn,
        String roaster,
        Integer acidity,
        Integer sweetness,
        Integer body,
        Integer roastLevel,
        String region,
        String roastLevelName,
        List<AromaType> tastingNotes,
        String imageUrl,
        String detailLink
) {
    /**
     * Entity + 관련 데이터 → DTO 변환
     * @param bean Bean 엔티티
     * @param roasterName 로스터 이름
     * @param imageUrl 대표 이미지 URL
     * @return BeanListItemDTO
     */
    public static BeanListItemDTO of(Bean bean, String roasterName, List<AromaType> aromaTypes, String imageUrl) {
        return new BeanListItemDTO(
                bean.getBeanId(),
                bean.getNameKo(),
                bean.getNameEn(),
                roasterName,
                bean.getAcidityPct(),
                bean.getSweetnessPct(),
                bean.getBodyPct(),
                bean.getRoastLevelPct(),
                bean.getRegion(),
                bean.getRoastLevel(),
                aromaTypes,
                imageUrl != null ? imageUrl : "/images/default-bean.png",
                "/api/beans/" + bean.getBeanId()
        );
    }
}
