package dripnote.bean.payload.dto;

import dripnote.bean.enums.RoastingType;
import lombok.Builder;

import java.util.List;

@Builder
public record ProductDetailDTO(
        ProductSummaryDTO beanSummary,
        RoasterDTO roaster,
        RoastingType roastingType,
        List<FlavorNoteDTO> flavorNotes,
        String description,
        Integer agtronMin,
        Integer agtronMax,
        Integer acidity,
        Integer sweetness,
        Integer body,
        Integer balance,
        List<ProductImageDTO> images

) {
}

