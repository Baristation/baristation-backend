package baristation.home.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import baristation.bean.domain.Product;

import java.util.List;

public record HomeProductResponse(
        @JsonProperty("product_name")
        String productName,

        @JsonProperty("product_flavor")
        List<String> productFlavor,

        @JsonProperty("product_image_link")
        String productImageLink,

        @JsonProperty("product_link")
        String productLink
) {
    public static HomeProductResponse of(Product product, List<String> flavors, String imageUrl) {
        return new HomeProductResponse(
                product.getNameKo(),
                flavors,
                imageUrl != null ? imageUrl : "/images/default-product.png",
                "/products/detail/" + product.getProductId()
        );
    }
}
