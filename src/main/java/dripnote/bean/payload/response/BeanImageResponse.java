package dripnote.bean.payload.response;

import dripnote.bean.domain.ProductImage;
import dripnote.bean.enums.ImageType;

public record BeanImageResponse(
        Long beanImageId,
        Long beanId,
        ImageType imageType,
        String imageUrl,
        Integer sortOrder
) {

    public static BeanImageResponse from(ProductImage productImage) {
        return new BeanImageResponse(
                productImage.getBeanImageId(),
                productImage.getBean().getBeanId(),
                productImage.getImageType(),
                productImage.getImageUrl(),
                productImage.getSortOrder()
        );
    }
}