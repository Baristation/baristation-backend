package dripnote.bean.payload.request;

import dripnote.bean.enums.AromaType;
import dripnote.bean.enums.RoastingType;

import java.util.List;

public record BeanSearchRequest(
        String keyword,        // 이름, 생산지 검색
        List<AromaType> aromas, // 다중 선택 필터링

        /**
         * 맛은 0 <= 맛 <= 5 사이로 주어짐.
         */

        // 1. 산미 (Acidity)
        Integer minAcidity,
        Integer maxAcidity,
        // 2. 단맛 (Sweetness)
        Integer minSweetness,
        Integer maxSweetness,

        // 3. 쓴맛 (Bitterness)
        Integer minBitterness,
        Integer maxBitterness,

        // 바디감
        Integer body,

        // 로스팅 정도
        RoastingType roastingType
) {}