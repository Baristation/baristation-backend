package baristation.bean.service;

import baristation.bean.domain.Product;
import baristation.bean.domain.ProductImage;
import baristation.bean.enums.ImageType;
import baristation.bean.payload.response.BeanImageResponse;
import baristation.bean.repository.ProductImageRepository;
import baristation.bean.repository.ProductRepository;
import baristation.common.exception.CustomException;
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

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final R2ImageService r2ImageService;

    private static final int THUMB_SORT_ORDER = 0;

    // 대표 이미지 업로드 또는 교체
    public BeanImageResponse uploadThumb(Long productId, MultipartFile file) throws IOException {
        Product product = getProduct(productId);

        ProductImage thumbImage = productImageRepository.findByProduct_ProductIdAndImageType(productId, ImageType.THUMB)
                .orElse(null);

        // 대표 이미지가 없으면 새로 저장
        if (thumbImage == null) {
                if (r2ImageService == null) {
                    throw new CustomException(INVALID_IMAGE_URL);
                }
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

        // 대표 이미지가 있으면 "기존 URL 경로 재사용"이 아니라
        // 항상 새 규칙 경로로 업로드
        String oldImageUrl = thumbImage.getImageUrl();

        String newImageUrl = r2ImageService.uploadBeanThumb(file, productId);

        // 예전 구버전 경로였다면 정리
        // 이미 새 경로였다면 uploadBeanThumb()가 같은 위치를 덮어썼을 수 있으므로 삭제하면 안 됨
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

        // 대표 이미지는 uploadThumb()을 통해 수정
        if (productImage.getImageType() == ImageType.THUMB) {
            throw new CustomException(THUMB_IMAGE_UPDATE_NOT_ALLOWED);
        }

        String oldImageUrl = productImage.getImageUrl();
        Long productId = productImage.getProduct().getProductId();

        String newImageUrl = r2ImageService.uploadBeanSubImage(file, productId);

        // 기존 파일 삭제
        if (r2ImageService != null) {
            r2ImageService.deleteByUrl(oldImageUrl);
        }

        productImage.changeImageUrl(newImageUrl);

        return toBeanImageResponse(productImage);
    }

    // 이미지 삭제
    public void deleteImage(Long beanImageId) {
        ProductImage productImage = productImageRepository.findById(beanImageId)
                .orElseThrow(() -> new CustomException(BEAN_IMAGE_NOT_FOUND));

        Long productId = productImage.getProduct().getProductId();
        boolean isSubImage = productImage.getImageType() == ImageType.SUB;

        if (r2ImageService != null) {
            r2ImageService.deleteByUrl(productImage.getImageUrl());
        }
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

    // 응답 DTO 변환
    private BeanImageResponse toBeanImageResponse(ProductImage productImage) {
        // DB에는 objectKey만 저장하고, 프론트 응답에는 public URL prefix를 붙여 내려줍니다.
        return new BeanImageResponse(
                productImage.getProductImageId(),
                productImage.getProduct().getProductId(),
                productImage.getImageType(),
                toPublicImageUrl(productImage.getImageUrl()),
                productImage.getSortOrder()
        );
    }

    private String toPublicImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return imageUrl;
        }

        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            return imageUrl;
        }

        return r2ImageService.buildPublicUrl(r2ImageService.extractObjectKey(imageUrl));
    }

    // Product 존재 여부 확인
    private Product getProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(BEAN_NOT_FOUND));
    }

    // 서브 이미지 정렬 재조정
    private void normalizeSubSortOrder(Long productId) {
        List<ProductImage> subImages = productImageRepository
                .findByProduct_ProductIdAndImageTypeOrderBySortOrderAsc(productId, ImageType.SUB);

        for (int i = 0; i < subImages.size(); i++) {
            subImages.get(i).changeSortOrder(i + 1);
        }
    }
}
