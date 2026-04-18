package dripnote.home.payload.response;

import lombok.Builder;
import dripnote.bean.payload.dto.BeanListItemDTO;

import java.util.List;

@Builder
public record HomeResponse(
        List<BeanListItemDTO> beans
) {
}
