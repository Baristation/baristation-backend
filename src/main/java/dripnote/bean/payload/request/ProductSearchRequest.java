package dripnote.bean.payload.request;

import dripnote.bean.enums.BeanSortType;
import dripnote.bean.enums.FlavorCategory;
import dripnote.bean.enums.RoastingType;

public record ProductSearchRequest(
        String keyword,        // 이름, 생산지 검색
        FlavorCategory flavorCategory, // Aroma -> FlavorCategory.

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
        RoastingType roastingType,

        // 정렬 기준
        BeanSortType sortBy
) {}