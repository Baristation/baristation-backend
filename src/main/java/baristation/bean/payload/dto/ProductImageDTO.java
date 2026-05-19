package baristation.bean.payload.dto;

import baristation.bean.enums.ImageType;
import lombok.Builder;

@Builder
public record ProductImageDTO(
        Long productImageId,
        ImageType imageType,
        String imageUrl,
        Integer sortOrder
) {
}