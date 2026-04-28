package baristation.bean.payload.dto;

import baristation.bean.domain.Product;
import baristation.bean.enums.FlavorCategory;
import baristation.bean.enums.RoastingType;

import java.util.List;

/**
 * 원두 목록 페이지 및 추천 리스트에서 사용되는 원두 DTO
 * Product 엔티티와 관련 데이터(맛, 이미지 등)를 조합한 응답 DTO
 */
public record ProductListItemDTO(
        Long productId,
        String productNameKo,
        String productNameEn,
        String roaster,
        Integer acidity,
        Integer sweetness,
        Integer body,
        Integer balance,
        RoastingType roastingType,
        List<FlavorCategory> flavorCategories,
        String imageUrl,
        String detailLink
) {
    /**
     * Entity + 관련 데이터 → DTO 변환
     * @param product Product 엔티티
     * @param roasterName 로스터 이름
     * @param imageUrl 대표 이미지 URL
     * @return BeanListItemDTO
     */
    public static ProductListItemDTO of(Product product, String roasterName, List<FlavorCategory> flavorCategories, String imageUrl) {
        return new ProductListItemDTO(
                product.getProductId(),
                product.getNameKo(),
                product.getNameEn(),
                roasterName,
                product.getAcidity(),
                product.getSweetness(),
                product.getBody(),
                product.getBalance(),
                product.getRoastLevel(),
                flavorCategories,
                imageUrl != null ? imageUrl : "/images/default-bean.png",
                "/api/products/" + product.getProductId()
        );
    }
}
