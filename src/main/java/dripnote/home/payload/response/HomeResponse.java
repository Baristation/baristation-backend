package dripnote.home.payload.response;

import lombok.Builder;
<<<<<<< HEAD
=======
import dripnote.bean.payload.dto.BeanSummaryDTO;
>>>>>>> f23511c (기능구현, 예외처리 미구현)

import java.util.List;

@Builder
public record HomeResponse(
<<<<<<< HEAD
        List<HomeFlavorResponse> flavors,
        List<HomeProductResponse> products
=======
        List<BeanSummaryDTO> beans
>>>>>>> f23511c (기능구현, 예외처리 미구현)
) {
}
