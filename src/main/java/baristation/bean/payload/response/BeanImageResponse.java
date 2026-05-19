package baristation.bean.payload.response;

import baristation.bean.domain.ProductImage;
import baristation.bean.enums.ImageType;
import lombok.Builder;

@Builder
public record BeanImageResponse(
        Long productImageId,
        Long productId,
        ImageType imageType,
        String imageUrl,
        Integer sortOrder
) {

    public static BeanImageResponse from(ProductImage productImage) {
        return new BeanImageResponse(
                productImage.getProductImageId(),
                productImage.getProduct().getProductId(),
                productImage.getImageType(),
                productImage.getImageUrl(),
                productImage.getSortOrder()
        );
    }
}