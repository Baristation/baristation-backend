package dripnote.bean.payload.dto;

import dripnote.bean.domain.Product;
import dripnote.bean.enums.FlavorNote;
import dripnote.bean.enums.RoastingType;

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
        List<FlavorNote> flavorNotes,
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
    public static ProductListItemDTO of(Product product, String roasterName, List<FlavorNote> flavorNotes, String imageUrl) {
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
                flavorNotes,
                imageUrl != null ? imageUrl : "/images/default-bean.png",
                "/api/products/" + product.getProductId()
        );
    }
}
