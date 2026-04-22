package dripnote.bean.payload.dto;

import dripnote.bean.enums.AromaType;

import java.util.List;

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
