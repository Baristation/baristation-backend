package baristation.bean.payload.dto;

import baristation.bean.domain.ProductImage;
import baristation.bean.enums.ImageType;
import lombok.Builder;

@Builder
public record ProductImageDTO(
        Long productImageId,
        ImageType imageType,
        String imageUrl,
        Integer sortOrder
) {
//    public static ProductImageDTO from(ProductImage productImage) {
//        return new ProductImageDTO(
//                productImage.getProductImageId(),
//                productImage.getImageType(),
//                productImage.getImageUrl(),
//                productImage.getSortOrder()
//        );
//    }
}