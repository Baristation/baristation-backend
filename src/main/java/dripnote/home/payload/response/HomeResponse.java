package dripnote.home.payload.response;

import lombok.Builder;

import java.util.List;

@Builder
public record HomeResponse(
        List<HomeFlavorResponse> flavors,
        List<HomeProductResponse> products
) {
}
