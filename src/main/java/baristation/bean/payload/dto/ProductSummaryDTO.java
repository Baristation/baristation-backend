package baristation.bean.payload.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record ProductSummaryDTO(
        Long productId,
        String beanNameKo,
        String beanNameEn,
        String origin,
        String region,
        String process,
        ProductImageDTO productImage,
        FlavorNoteDTO flavorNotes
) {

}
