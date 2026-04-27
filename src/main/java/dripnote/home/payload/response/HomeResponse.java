package dripnote.home.payload.response;

import lombok.Builder;
import dripnote.bean.payload.dto.ProductSummaryDTO;

import java.util.List;

@Builder
public record HomeResponse(
        List<ProductSummaryDTO> beans
) {
}
