package dripnote.bean.payload.dto;

import lombok.Builder;

@Builder
public record BeanSummaryDTO(
        Long beanId,
        String beanNameKo,
        String beanNameEn,
        String origin,
        String region,
        String process,
        ProductImageDTO productImage
) {

}
