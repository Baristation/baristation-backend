package dripnote.bean.payload.response;

import dripnote.bean.domain.BeanImage;
import dripnote.bean.enums.ImageType;
import lombok.Builder;

@Builder
public record BeanImageResponse(
        Long beanImageId,
        Long beanId,
        ImageType imageType,
        String imageUrl,
        Integer sortOrder
) {
    public static BeanImageResponse from(BeanImage beanImage) {
        return new BeanImageResponse(
                beanImage.getBeanImageId(),
                beanImage.getBean().getBeanId(),
                beanImage.getImageType(),
                beanImage.getImageUrl(),
                beanImage.getSortOrder()
        );
    }
}