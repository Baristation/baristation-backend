package baristation.bean.service;

import baristation.bean.domain.Bean;
import baristation.bean.domain.BeanProduct;
import baristation.bean.domain.Product;
import baristation.bean.enums.ImageType;
import baristation.bean.payload.dto.*;
import baristation.bean.payload.request.ProductSearchRequest;
import baristation.bean.repository.BeanProductRepository;
import baristation.bean.repository.ProductFlavorNoteRepository;
import baristation.bean.repository.ProductImageRepository;
import baristation.common.exception.CustomException;
import baristation.common.exception.ErrorCode;
import baristation.common.payload.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * BeanService 구현체
 * 원두 검색 및 목록 조회 기능을 담당
 */

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BeanServiceImpl implements BeanService {

    private final BeanProductRepository beanProductRepository;
    private final ProductFlavorNoteRepository productFlavorNoteRepository;
    private final ProductImageRepository productImageRepository;

    /**
     * 조건에 맞는 원두 목록을 검색하고 페이지네이션하여 반환
     * 
     * @param request 검색 필터 조건 (키워드, 맛, 로스팅 정도 등)
     * @param pageable 페이지네이션 정보
     * @return 페이지네이션된 BeanSummaryDTO 응답
     */
    @Override
    public PageResponse<ProductSummaryDTO> searchProducts(ProductSearchRequest request, Pageable pageable) {
        // 조건 검증
        validateSearchRequest(request);
        if (pageable == null) {
            throw new CustomException(ErrorCode.BEAN_SEARCH_FAILED);
        }

        Page<BeanProduct> beanProductPage = beanProductRepository.searchBeansWithFilters(request, pageable);
        if (beanProductPage == null) {
            throw new CustomException(ErrorCode.BEAN_SEARCH_FAILED);
        }

        List<Long> productIds = beanProductPage.getContent().stream()
                .map(BeanProduct::getProduct)
                .filter(Objects::nonNull)
                .map(Product::getProductId)
                .filter(Objects::nonNull)
                .toList();

        Map<Long, ProductImageDTO> thumbImageByProductId = getThumbImages(productIds);

        Page<ProductSummaryDTO> page = beanProductPage.map(beanProduct -> {
            if (beanProduct.getBean() == null || beanProduct.getProduct() == null || beanProduct.getProduct().getProductId() == null) {
                throw new CustomException(ErrorCode.BEAN_SEARCH_FAILED);
            }
            return toSummaryDto(
                    new ListItemSource(beanProduct.getBean(), beanProduct.getProduct()),
                    thumbImageByProductId.get(beanProduct.getProduct().getProductId())
            );
        });

        return PageResponse.of(page);
    }
    @Override
    public ProductDetailDTO getProductDetail(Long productId) {

        BeanProduct selectedBeanProduct = beanProductRepository.findByProductId(productId);

        if (selectedBeanProduct == null || selectedBeanProduct.getBean() == null || selectedBeanProduct.getProduct() == null) {
            throw new CustomException(ErrorCode.BEAN_NOT_FOUND);
        }

        Product product = selectedBeanProduct.getProduct();
        Bean bean = selectedBeanProduct.getBean();
        Long resolvedProductId = product.getProductId();

        // 2. 상세 응답용 전체 이미지와 대표 썸네일을 분리 조회
        List<ProductImageDTO> images = productImageRepository.findByProduct_ProductIdOrderBySortOrderAsc(resolvedProductId)
                .stream()
                .map(ProductImageDTO::from)
                .filter(image -> image.imageType() != ImageType.THUMB)
                .toList();

        ProductImageDTO thumbImage = productImageRepository.findByProduct_ProductIdAndImageTypeOrderBySortOrderAsc(
                        resolvedProductId,
                        ImageType.THUMB
                ).stream()
                .findFirst()
                .map(ProductImageDTO::from)
                .orElse(null);

        List<FlavorNoteDTO> flavorNotes = productFlavorNoteRepository.findByProduct_ProductIdIn(List.of(resolvedProductId)).stream()
                .map(productFlavorNote -> FlavorNoteDTO.from(productFlavorNote.getFlavorNote()))
                .toList();

        // 3. 상세 DTO 조립
        ProductSummaryDTO summary = ProductSummaryDTO.builder()
                .beanId(bean.getBeanId())
                .beanNameKo(bean.getNameKo())
                .beanNameEn(bean.getNameEn())
                .origin(bean.getOrigin())
                .region(bean.getRegion())
                .process(bean.getProcess())
                .productImage(thumbImage)
                .build();

        return ProductDetailDTO.builder()
                .beanSummary(summary)
                .roaster(RoasterDTO.from(product.getRoaster()))
                .roastingType(product.getRoastLevel())
                .flavorNotes(flavorNotes)
                .description(product.getDescription())
                .agtronMin(product.getAgtronMin())
                .agtronMax(product.getAgtronMax())
                .acidity(product.getAcidity())
                .sweetness(product.getSweetness())
                .body(product.getBody())
                .balance(product.getBalance())
                .images(images)
                .build();
    }
    private Map<Long, ProductImageDTO> getThumbImages(List<Long> productIds) {
        if (productIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return productImageRepository.findByProduct_ProductIdInAndImageType(productIds, ImageType.THUMB)
                .stream()
                .collect(Collectors.toMap(
                        image -> image.getProduct().getProductId(),
                        ProductImageDTO::from,
                        (first, second) -> first
                ));
    }

    private ProductSummaryDTO toSummaryDto(ListItemSource source, ProductImageDTO image) {
        Bean bean = source.bean();

        return ProductSummaryDTO.builder()
                .beanId(bean.getBeanId())
                .beanNameKo(bean.getNameKo())
                .beanNameEn(bean.getNameEn())
                .origin(bean.getOrigin())
                .region(bean.getRegion())
                .process(bean.getProcess())
                .productImage(image)
                .build();
    }

    private void validateSearchRequest(ProductSearchRequest request) {
        if (request == null) {
            return;
        }

        validateRange(request.minAcidity(), request.maxAcidity());
        validateRange(request.minSweetness(), request.maxSweetness());
        validateRange(request.minBitterness(), request.maxBitterness());
    }

    private void validateRange(Integer min, Integer max) {
        if (min != null && max != null && min > max) {
            throw new CustomException(ErrorCode.BEAN_SEARCH_INVALID_RANGE);
        }
    }

}

