package dripnote.bean.service;

import dripnote.bean.enums.AromaType;
import dripnote.bean.enums.RoastingType;
import dripnote.bean.payload.dto.ProductListItemDTO;
import dripnote.bean.payload.request.BeanSearchRequest;
import dripnote.common.payload.response.PageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@Profile("test")
public class MockBeanServiceImpl implements BeanService {

    private static final List<ProductListItemDTO> MOCK_DB = new ArrayList<>();

    static {
        AromaType[] aromas = AromaType.values();
        for (int i = 1; i <= 30; i++) {
            RoastingType roasting = switch (i % 3) {
                case 0 -> RoastingType.DARK;
                case 1 -> RoastingType.LIGHT;
                default -> RoastingType.MEDIUM;
            };
            int bitternessScore = switch (roasting) {
                case LIGHT -> 2;
                case MEDIUM -> 3;
                case DARK -> 4;
                case MEDIUMLIGHT -> 5;
                case MEDIUMDARK -> 6;
            };

            MOCK_DB.add(new ProductListItemDTO(
                    (long) i,
                    "테스트 원두 " + i,
                    "Mock Bean " + i,
                    "Mock Roaster " + ((i % 5) + 1),
                    (i % 5) + 1,
                    ((i + 1) % 5) + 1,
                    ((i + 2) % 5) + 1,
                    bitternessScore,
                    i % 2 == 0 ? "Ethiopia" : "Colombia",
                    roasting.name(),
                    List.of(aromas[i % aromas.length]),
                    "https://example.com/mock/bean-" + i + ".jpg",
                    "/api/beans/" + i
            ));
        }
    }

    @Override
    public PageResponse<ProductListItemDTO> searchBeans(BeanSearchRequest request, Pageable pageable) {
        log.info("Mock BeanService 호출됨: {}", request);

        List<ProductListItemDTO> filtered = MOCK_DB.stream()
                .filter(bean -> matches(bean, request))
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filtered.size());

        List<ProductListItemDTO> pageContent;
        if (start >= filtered.size()) {
            pageContent = new ArrayList<>(); // 범위를 벗어나면 빈 리스트 반환
        } else {
            pageContent = filtered.subList(start, end);
        }
        // Page 객체 생성 후 PageResponse로 변환
        Page<ProductListItemDTO> page = new PageImpl<>(pageContent, pageable, filtered.size());
        return PageResponse.of(page);
    }

    @Override
    public ProductListItemDTO getBeanDetail(Long beanId) {
        return null;
    }

    private boolean matches(ProductListItemDTO bean, BeanSearchRequest request) {
        if (request == null) {
            return true;
        }
        return matchKeyword(bean, request.keyword())
                && matchAromas(bean, request.aromas())
                && matchRange(bean.acidity(), request.minAcidity(), request.maxAcidity())
                && matchRange(bean.sweetness(), request.minSweetness(), request.maxSweetness())
                && matchRange(bean.roastLevel(), request.minBitterness(), request.maxBitterness())
                && matchBody(bean, request.body())
                && matchRoasting(bean, request.roastingType());
    }

    private boolean matchKeyword(ProductListItemDTO bean, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        String q = keyword.toLowerCase(Locale.ROOT);
        return (bean.productNameKo() != null && bean.productNameKo().toLowerCase(Locale.ROOT).contains(q))
                || (bean.productNameEn() != null && bean.productNameEn().toLowerCase(Locale.ROOT).contains(q))
                || (bean.region() != null && bean.region().toLowerCase(Locale.ROOT).contains(q));
    }

    private boolean matchAromas(ProductListItemDTO bean, List<AromaType> aromas) {
        if (aromas == null || aromas.isEmpty()) {
            return true;
        }
        return bean.tastingNotes() != null && bean.tastingNotes().stream().anyMatch(aromas::contains);
    }

    private boolean matchRange(Integer value, Integer min, Integer max) {
        if (value == null) {
            return min == null && max == null;
        }
        if (min != null && value < min) {
            return false;
        }
        return max == null || value <= max;
    }

    private boolean matchBody(ProductListItemDTO bean, Integer body) {
        return body == null || (bean.body() != null && bean.body().equals(body));
    }

    private boolean matchRoasting(ProductListItemDTO bean, RoastingType roastingType) {
        return roastingType == null || roastingType.name().equalsIgnoreCase(bean.roastingType());
    }
}