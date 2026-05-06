package baristation.home.service;

import baristation.bean.domain.FlavorNote;
import baristation.bean.domain.Product;
import baristation.bean.domain.ProductFlavorNote;
import baristation.bean.domain.ProductImage;
import baristation.bean.enums.ImageType;
import baristation.bean.repository.FlavorNoteRepository;
import baristation.bean.repository.ProductFlavorNoteRepository;
import baristation.bean.repository.ProductImageRepository;
import baristation.bean.repository.ProductRepository;
import baristation.home.payload.response.HomeFlavorResponse;
import baristation.home.payload.response.HomeProductResponse;
import baristation.home.payload.response.HomeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class HomeServiceImpl implements HomeService {

    @Value("${cloudflare.r2.public-base-url}")
    private String publicBaseUrl;

    private final ProductRepository productRepository;
    private final FlavorNoteRepository flavorNoteRepository;
    private final ProductFlavorNoteRepository productFlavorNoteRepository;
    private final ProductImageRepository productImageRepository;

    /**
     * 향미 바로가기와 추천 상품 카드를 조합해 메인 페이지 응답을 생성합니다.
     */
    @Override
    public HomeResponse getHome() {
        List<HomeFlavorResponse> flavors = getFlavors();
        List<HomeProductResponse> products = getProducts();

        return HomeResponse.builder()
                .flavors(flavors)
                .products(products)
                .build();
    }

    /**
     * 메인 페이지 상단에 노출할 향미 바로가기 목록을 조회합니다.
     */
    private List<HomeFlavorResponse> getFlavors() {
        List<FlavorNote> flavorNotes = flavorNoteRepository.findTop8ByOrderByFlavorNoteIdAsc();

        return flavorNotes.stream()
                .map(flavorNote -> HomeFlavorResponse.of(
                        flavorNote,
                        buildImageUrl(flavorNote.getFlavorImageUrl())
                ))
                .toList();
    }

    /**
     * 최신 추천 상품 목록과 각 상품의 향미, 썸네일 이미지를 함께 조회합니다.
     */
    private List<HomeProductResponse> getProducts() {
        List<Product> products = productRepository.findTop4ByOrderByCreatedAtDesc();

        if (products.isEmpty()) {
            return List.of();
        }

        List<Long> productIds = products.stream()
                .map(Product::getProductId)
                .toList();

        Map<Long, List<String>> productFlavorMap = getProductFlavorMap(productIds);
        Map<Long, String> productImageMap = getProductImageMap(productIds);

        return products.stream()
                .map(product -> HomeProductResponse.of(
                        product,
                        productFlavorMap.getOrDefault(product.getProductId(), List.of()),
                        productImageMap.get(product.getProductId())
                ))
                .toList();
    }

    /**
     * 상품마다 향미를 다시 조회하지 않도록 상품 ID 기준으로 향미 이름을 묶습니다.
     */
    private Map<Long, List<String>> getProductFlavorMap(List<Long> productIds) {
        List<ProductFlavorNote> productFlavorNotes =
                productFlavorNoteRepository.findByProduct_ProductIdIn(productIds);

        Map<Long, List<String>> productFlavorMap = new LinkedHashMap<>();
        for (ProductFlavorNote productFlavorNote : productFlavorNotes) {
            Long productId = productFlavorNote.getProduct().getProductId();
            String flavorName = productFlavorNote.getFlavorNote().getNameKo();

            productFlavorMap
                    .computeIfAbsent(productId, key -> new ArrayList<>())
                    .add(flavorName);
        }

        return productFlavorMap;
    }

    /**
     * 상품별 대표 썸네일 이미지를 하나씩 선택합니다.
     */
    private Map<Long, String> getProductImageMap(List<Long> productIds) {
        List<ProductImage> productImages =
                productImageRepository.findByProduct_ProductIdInAndImageType(productIds, ImageType.THUMB);

        Map<Long, String> productImageMap = new LinkedHashMap<>();
        for (ProductImage productImage : productImages) {
            Long productId = productImage.getProduct().getProductId();
            productImageMap.putIfAbsent(productId, productImage.getImageUrl());
        }

        return productImageMap;
    }

    // flavor ImageUrl 조립
    private String buildImageUrl(String imagePath) {
        if (imagePath == null || imagePath.isBlank()) {
            return null;
        }

        if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
            return imagePath;
        }

        String baseUrl = publicBaseUrl.endsWith("/")
                ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1)
                : publicBaseUrl;

        String path = imagePath.startsWith("/")
                ? imagePath
                : "/" + imagePath;

        return baseUrl + path;
    }
}
