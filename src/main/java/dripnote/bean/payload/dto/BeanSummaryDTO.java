package dripnote.bean.payload.dto;

import dripnote.bean.enums.AromaType;

import java.util.List;

/**
 *
 * ==========원두 목록 페이지=========
 *
 * productId
 * nameKo
 * nameEn
 * flavor
 * roastLevel
 * acidity
 * sweetness
 * body
 * balance
 *
 * Bean(origin region)
 *
 * ProductImage (id, imageUrl, Thumb)
 *
 * FlavorNote (flavorNoteId, flavorCategory, nameKo, flavorImageUrl)
 *
 * ==========원두 상세 페이지========
 *
 * 위 내용 +
 *
 * agtronMin
 * agtronMax
 * description
 */

public record BeanSummaryDTO(
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

}
