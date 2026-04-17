package dripnote.home.payload.response;

import lombok.Builder;
import dripnote.home.payload.dto.HomeBeanDTO;
import dripnote.home.payload.dto.HomeTastingsDTO;

import java.util.List;

@Builder
public record HomeResponse(
        List<HomeBeanDTO> beans
) {
}
