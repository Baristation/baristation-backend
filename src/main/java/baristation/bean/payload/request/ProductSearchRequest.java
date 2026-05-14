package baristation.bean.payload.request;

import baristation.bean.enums.BeanSortType;
import baristation.bean.enums.FlavorCategory;
import baristation.bean.enums.RoastingType;

public record ProductSearchRequest(
        String keyword,        // 이름, 생산지 검색
        FlavorCategory flavorCategory, // Aroma -> FlavorCategory.

        // 1. 산미 (Acidity)
        Double minAcidity,
        Double maxAcidity,
        // 2. 단맛 (Sweetness)
        Double minSweetness,
        Double maxSweetness,

        // 바디감
        Double minBody,
        Double maxBody,

        // 밸런스
        Double minBalance,
        Double maxBalance,

        // 로스팅 정도
        RoastingType roastingType,

        // 정렬 기준
        BeanSortType sortBy
) {}