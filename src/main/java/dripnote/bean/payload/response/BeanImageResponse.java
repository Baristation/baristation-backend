package dripnote.bean.payload.response;

import dripnote.bean.domain.ProductImage;
import dripnote.bean.enums.ImageType;

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