package dripnote.bean.payload.dto;

import dripnote.bean.domain.ProductImage;
import dripnote.bean.enums.ImageType;

public record ProductImageDTO(
        Long productImageId,
        ImageType imageType,
        String imageUrl,
        Integer sortOrder
) {
    public static ProductImageDTO from(ProductImage productImage) {
        return new ProductImageDTO(
                productImage.getProductImageId(),
                productImage.getImageType(),
                productImage.getImageUrl(),
                productImage.getSortOrder()
        );
    }
}