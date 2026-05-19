package baristation.bean.service;

import baristation.bean.domain.Product;
import baristation.bean.domain.ProductImage;
import baristation.bean.enums.ImageType;
import baristation.bean.payload.response.BeanImageResponse;
import baristation.bean.repository.ProductImageRepository;
import baristation.bean.repository.ProductRepository;
import baristation.common.exception.CustomException;
import baristation.common.r2.ImageUrlResolver;
import baristation.common.r2.R2ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static baristation.common.exception.ErrorCode.*;

@Service
@Transactional
@RequiredArgsConstructor
public class BeanImageServiceImpl {

    private static final int THUMB_SORT_ORDER = 0;

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final R2ImageService r2ImageService;
    private final ImageUrlResolver imageUrlResolver;

    // 대표 이미지 업로드 또는 교체
    public BeanImageResponse uploadThumb(Long productId, MultipartFile file) throws IOException {
        Product product = getProduct(productId);

        ProductImage thumbImage = productImageRepository.findByProduct_ProductIdAndImageType(productId, ImageType.THUMB)
                .orElse(null);

        if (thumbImage == null) {
            String imageUrl = r2ImageService.uploadBeanThumb(file, productId);

            ProductImage saved = productImageRepository.save(
                    ProductImage.builder()
                            .product(product)
                            .imageType(ImageType.THUMB)
                            .imageUrl(imageUrl)
                            .sortOrder(THUMB_SORT_ORDER)
                            .build()
            );

            return toBeanImageResponse(saved);
        }

        String oldImageUrl = thumbImage.getImageUrl();
        String newImageUrl = r2ImageService.uploadBeanThumb(file, productId);

        if (!oldImageUrl.equals(newImageUrl)) {
            r2ImageService.deleteByUrl(oldImageUrl);
        }

        thumbImage.changeImageUrl(newImageUrl);
        thumbImage.changeSortOrder(THUMB_SORT_ORDER);

        return toBeanImageResponse(thumbImage);
    }

    // 서브 이미지 추가
    public BeanImageResponse uploadSub(Long productId, MultipartFile file) throws IOException {
        Product product = getProduct(productId);
        int nextSortOrder = productImageRepository.findMaxSubSortOrder(productId) + 1;
        String imageUrl = r2ImageService.uploadBeanSubImage(file, productId);

        ProductImage subImage = ProductImage.builder()
                .product(product)
                .imageType(ImageType.SUB)
                .imageUrl(imageUrl)
                .sortOrder(nextSortOrder)
                .build();

        ProductImage saved = productImageRepository.save(subImage);

        return toBeanImageResponse(saved);
    }

    // 서브 이미지 교체
    public BeanImageResponse updateImage(Long beanImageId, MultipartFile file) throws IOException {
        ProductImage productImage = productImageRepository.findById(beanImageId)
                .orElseThrow(() -> new CustomException(BEAN_IMAGE_NOT_FOUND));

        if (productImage.getImageType() == ImageType.THUMB) {
            throw new CustomException(THUMB_IMAGE_UPDATE_NOT_ALLOWED);
        }

        String oldImageUrl = productImage.getImageUrl();
        Long productId = productImage.getProduct().getProductId();
        String newImageUrl = r2ImageService.uploadBeanSubImage(file, productId);

        r2ImageService.deleteByUrl(oldImageUrl);
        productImage.changeImageUrl(newImageUrl);

        return toBeanImageResponse(productImage);
    }

    // 이미지 삭제
    public void deleteImage(Long beanImageId) {
        ProductImage productImage = productImageRepository.findById(beanImageId)
                .orElseThrow(() -> new CustomException(BEAN_IMAGE_NOT_FOUND));

        Long productId = productImage.getProduct().getProductId();
        boolean isSubImage = productImage.getImageType() == ImageType.SUB;

        r2ImageService.deleteByUrl(productImage.getImageUrl());
        productImageRepository.delete(productImage);

        if (isSubImage) {
            normalizeSubSortOrder(productId);
        }
    }

    // 원두 이미지 조회
    @Transactional(readOnly = true)
    public List<BeanImageResponse> getImages(Long productId) {
        getProduct(productId);

        return productImageRepository.findByProduct_ProductIdOrderBySortOrderAsc(productId)
                .stream()
                .map(this::toBeanImageResponse)
                .toList();
    }

    private BeanImageResponse toBeanImageResponse(ProductImage productImage) {
        // DB에는 objectKey만 저장하고, 프론트 응답에는 공통 컴포넌트로 public URL prefix를 붙입니다.
        return new BeanImageResponse(
                productImage.getProductImageId(),
                productImage.getProduct().getProductId(),
                productImage.getImageType(),
                imageUrlResolver.toPublicUrl(productImage.getImageUrl()),
                productImage.getSortOrder()
        );
    }

    private Product getProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(BEAN_NOT_FOUND));
    }

    private void normalizeSubSortOrder(Long productId) {
        List<ProductImage> subImages = productImageRepository
                .findByProduct_ProductIdAndImageTypeOrderBySortOrderAsc(productId, ImageType.SUB);

        for (int i = 0; i < subImages.size(); i++) {
            subImages.get(i).changeSortOrder(i + 1);
        }
    }
}
