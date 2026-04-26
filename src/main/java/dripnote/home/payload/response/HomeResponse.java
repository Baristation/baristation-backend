package dripnote.home.payload.response;

import lombok.Builder;
import dripnote.bean.payload.dto.BeanSummaryDTO;

import java.util.List;

@Builder
public record HomeResponse(
        List<BeanSummaryDTO> beans
) {
}
