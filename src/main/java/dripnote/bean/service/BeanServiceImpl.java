package dripnote.bean.service;

import dripnote.bean.domain.Bean;
import dripnote.bean.domain.BeanProduct;
import dripnote.bean.domain.Product;
import dripnote.bean.domain.ProductFlavorNote;
import dripnote.bean.enums.BeanSortType;
import dripnote.bean.enums.FlavorCategory;
import dripnote.bean.enums.ImageType;
import dripnote.bean.enums.RoastingType;
import dripnote.bean.payload.dto.*;
import dripnote.bean.payload.request.BeanSearchRequest;
import dripnote.bean.repository.BeanProductRepository;
import dripnote.bean.repository.ProductImageRepository;
import dripnote.bean.repository.ProductFlavorNoteRepository;
import dripnote.common.payload.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * BeanService 구현체
 * 원두 검색 및 목록 조회 기능을 담당
 */

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BeanServiceImpl implements BeanService {

    private final BeanProductRepository beanProductRepository;
    private final ProductFlavorNoteRepository productFlavorNoteRepository;
    private final ProductImageRepository productImageRepository;

    /**
     * 조건에 맞는 원두 목록을 검색하고 페이지네이션하여 반환
     * 
     * @param request 검색 필터 조건 (키워드, 맛, 로스팅 정도 등)
     * @param pageable 페이지네이션 정보
     * @return 페이지네이션된 BeanSummaryDTO 응답
     */
    @Override
    public PageResponse<BeanSummaryDTO> searchBeans(BeanSearchRequest request, Pageable pageable) {
        List<BeanProduct> beanProducts = beanProductRepository.findAllWithBeanAndProductAndRoaster();
        List<ListItemSource> sourceItems = reduceByBean(beanProducts);

        List<Long> productIds = sourceItems.stream()
                .map(item -> item.product().getProductId())
                .toList();

        Map<Long, ProductImageDTO> thumbImageByProductId = getThumbImages(productIds);
        Map<Long, List<FlavorNoteDTO>> flavorNotesByProductId = getFlavorNotes(productIds);

        List<ListItemSource> filteredAndSorted = sourceItems.stream()
                .filter(item -> matches(item, request, flavorNotesByProductId.getOrDefault(item.product().getProductId(), List.of())))
                .sorted(resolveComparator(request, pageable))
                .toList();

        int start = Math.min((int) pageable.getOffset(), filteredAndSorted.size());
        int end = Math.min(start + pageable.getPageSize(), filteredAndSorted.size());

        List<BeanSummaryDTO> pageContent = filteredAndSorted.subList(start, end).stream()
                .map(item -> toSummaryDto(
                        item,
                        thumbImageByProductId.get(item.product().getProductId())
                ))
                .toList();

        Page<BeanSummaryDTO> page = new PageImpl<>(pageContent, pageable, filteredAndSorted.size());
        return PageResponse.of(page);
    }

    @Override
    public BeanDetailDTO getBeanDetail(Long beanId) {
        List<BeanProduct> beanProducts = beanProductRepository.findAllWithBeanAndProductAndRoaster();
        List<ListItemSource> sourceItems = reduceByBean(beanProducts);

        Map<Long, List<FlavorNoteDTO>> flavorNotesByProductId = getFlavorNotes(
                sourceItems.stream().map(item -> item.product().getProductId()).toList()
        );
        Map<Long, ProductImageDTO> thumbImageByProductId = getThumbImages(
                sourceItems.stream().map(item -> item.product().getProductId()).toList()
        );

        ListItemSource selected = sourceItems.stream()
                .filter(item -> Objects.equals(item.bean().getBeanId(), beanId))
                .findFirst()
                .orElse(null);

        if (selected == null) {
            return null;
        }

        Product product = selected.product();
        Bean bean = selected.bean();

        List<ProductImageDTO> images = productImageRepository.findByProduct_ProductIdOrderBySortOrderAsc(product.getProductId())
                .stream()
                .map(ProductImageDTO::from)
                .toList();

        List<FlavorNoteDTO> flavorNotes = flavorNotesByProductId.getOrDefault(product.getProductId(), List.of());

        BeanSummaryDTO summary = BeanSummaryDTO.builder()
                .beanId(bean.getBeanId())
                .beanNameKo(bean.getNameKo())
                .beanNameEn(bean.getNameEn())
                .origin(bean.getOrigin())
                .region(bean.getRegion())
                .process(bean.getProcess())
                .productImage(thumbImageByProductId.get(product.getProductId()))
                .build();

        return BeanDetailDTO.builder()
                .beanSummary(summary)
                .roaster(RoasterDTO.from(product.getRoaster()))
                .roastingType(product.getRoastLevel())
                .flavorNotes(flavorNotes)
                .description(product.getDescription())
                .agtronMin(product.getAgtronMin())
                .agtronMax(product.getAgtronMax())
                .acidity(product.getAcidity())
                .sweetness(product.getSweetness())
                .body(product.getBody())
                .balance(product.getBalance())
                .images(images)
                .build();
    }

    private List<ListItemSource> reduceByBean(List<BeanProduct> beanProducts) {
        Comparator<BeanProduct> latestProductComparator = Comparator
                .comparing((BeanProduct value) -> value.getProduct().getCreatedAt(), Comparator.nullsLast(Comparator.naturalOrder()))
                .reversed()
                .thenComparing(value -> value.getProduct().getProductId(), Comparator.nullsLast(Comparator.reverseOrder()));

        Map<Long, ListItemSource> byBeanId = beanProducts.stream()
                .sorted(latestProductComparator)
                .collect(Collectors.toMap(
                        bp -> bp.getBean().getBeanId(),
                        bp -> new ListItemSource(bp.getBean(), bp.getProduct()),
                        (existing, replacement) -> existing, // 중복 시 기존 것 유지 (최신 기준)
                        LinkedHashMap::new
                ));

        return new ArrayList<>(byBeanId.values());
    }

    private Map<Long, ProductImageDTO> getThumbImages(List<Long> productIds) {
        if (productIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return productImageRepository.findByProduct_ProductIdInAndImageType(productIds, ImageType.THUMB)
                .stream()
                .collect(Collectors.toMap(
                        image -> image.getProduct().getProductId(),
                        ProductImageDTO::from,
                        (first, second) -> first
                ));
    }

    // 개선 제안: Stream groupingBy 활용
    private Map<Long, List<FlavorNoteDTO>> getFlavorNotes(List<Long> productIds) {
        if (productIds.isEmpty()) return Collections.emptyMap();

        return productFlavorNoteRepository.findByProduct_ProductIdIn(productIds).stream()
                .collect(Collectors.groupingBy(
                        pfn -> pfn.getProduct().getProductId(),
                        Collectors.mapping(
                                pfn -> FlavorNoteDTO.from(pfn.getFlavorNote()),
                                Collectors.toList()
                        )
                ));
    }
    private BeanSummaryDTO toSummaryDto(ListItemSource source, ProductImageDTO image) {
        Bean bean = source.bean();

        return BeanSummaryDTO.builder()
                .beanId(bean.getBeanId())
                .beanNameKo(bean.getNameKo())
                .beanNameEn(bean.getNameEn())
                .origin(bean.getOrigin())
                .region(bean.getRegion())
                .process(bean.getProcess())
                .productImage(image)
                .build();
    }

    private boolean matches(ListItemSource source, BeanSearchRequest request, List<FlavorNoteDTO> flavorNotes) {
        if (request == null) {
            return true;
        }

        return matchKeyword(source, flavorNotes, request.keyword())
                && matchFlavorCategory(flavorNotes, request.flavorCategory())
                && matchRange(source.product().getAcidity(), request.minAcidity(), request.maxAcidity())
                && matchRange(source.product().getSweetness(), request.minSweetness(), request.maxSweetness())
                && matchRange(toBitternessScore(source.product().getRoastLevel()), request.minBitterness(), request.maxBitterness())
                && matchBody(source.product().getBody(), request.body())
                && matchRoasting(source.product().getRoastLevel(), request.roastingType());
    }

    private boolean matchKeyword(ListItemSource source, List<FlavorNoteDTO> flavorNotes, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }

        String normalized = keyword.trim().toLowerCase(Locale.ROOT);
        Bean bean = source.bean();
        Product product = source.product();

        boolean matchesBean = containsIgnoreCase(bean.getNameKo(), normalized)
                || containsIgnoreCase(bean.getNameEn(), normalized);
        boolean matchesRoaster = product.getRoaster() != null
                && (containsIgnoreCase(product.getRoaster().getNameKo(), normalized)
                || containsIgnoreCase(product.getRoaster().getNameEn(), normalized));
        boolean matchesFlavor = flavorNotes.stream().anyMatch(note ->
                containsIgnoreCase(note.nameKo(), normalized) || containsIgnoreCase(note.nameEn(), normalized)
        );

        return matchesBean || matchesRoaster || matchesFlavor;
    }

    private boolean matchFlavorCategory(List<FlavorNoteDTO> flavorNotes, FlavorCategory flavorCategory) {
        if (flavorCategory == null) {
            return true;
        }

        return flavorNotes.stream()
                .map(FlavorNoteDTO::flavorCategory)
                .anyMatch(flavorCategory::equals);
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

    private boolean matchBody(Integer value, Integer body) {
        return body == null || Objects.equals(value, body);
    }

    private boolean matchRoasting(RoastingType value, RoastingType expected) {
        return expected == null || expected == value;
    }

    private int toBitternessScore(RoastingType roastingType) {
        if (roastingType == null) {
            return 0;
        }

        return switch (roastingType) {
            case LIGHT -> 1;
            case MEDIUMLIGHT -> 2;
            case MEDIUM -> 3;
            case MEDIUMDARK -> 4;
            case DARK -> 5;
        };
    }

    private Comparator<ListItemSource> resolveComparator(BeanSearchRequest request, Pageable pageable) {
        BeanSortType sortType = request != null && request.sortBy() != null
                ? request.sortBy()
                : BeanSortType.LATEST;

        Comparator<ListItemSource> comparator = comparatorBy(sortType);

        if (request != null && request.sortBy() == null && pageable.getSort().isSorted()) {
            Sort.Order order = pageable.getSort().stream().findFirst().orElse(null);
            if (order != null) {
                comparator = comparatorFromPageableSort(order);
            }
        }

        return comparator;
    }

    private Comparator<ListItemSource> comparatorFromPageableSort(Sort.Order order) {
        Comparator<ListItemSource> comparator = switch (order.getProperty()) {
            case "name", "beanNameKo" -> comparatorBy(BeanSortType.NAME);
            case "roastingLevel", "roastLevel" -> comparatorBy(BeanSortType.ROASTING_LEVEL);
            case "acidity" -> comparatorBy(BeanSortType.ACIDITY);
            case "body" -> comparatorBy(BeanSortType.BODY);
            default -> comparatorBy(BeanSortType.LATEST);
        };

        return order.getDirection() == Sort.Direction.ASC ? comparator : comparator.reversed();
    }

    private Comparator<ListItemSource> comparatorBy(BeanSortType sortType) {
        return switch (sortType) {
            case NAME -> Comparator
                    .comparing((ListItemSource item) -> nullSafe(item.bean().getNameKo()))
                    .thenComparing(item -> nullSafe(item.bean().getNameEn()));
            case ROASTING_LEVEL -> Comparator
                    .comparingInt((ListItemSource item) -> roastingRank(item.product().getRoastLevel()))
                    .thenComparing(item -> nullSafe(item.bean().getNameKo()));
            case ACIDITY -> Comparator
                    .comparing((ListItemSource item) -> item.product().getAcidity(), Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(item -> nullSafe(item.bean().getNameKo()));
            case BODY -> Comparator
                    .comparing((ListItemSource item) -> item.product().getBody(), Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(item -> nullSafe(item.bean().getNameKo()));
            case LATEST -> Comparator
                    .comparing((ListItemSource item) -> item.product().getCreatedAt(), Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(item -> item.product().getProductId(), Comparator.nullsLast(Comparator.reverseOrder()));
        };
    }

    private int roastingRank(RoastingType roastingType) {
        if (roastingType == null) {
            return Integer.MAX_VALUE;
        }

        return switch (roastingType) {
            case LIGHT -> 1;
            case MEDIUMLIGHT -> 2;
            case MEDIUM -> 3;
            case MEDIUMDARK -> 4;
            case DARK -> 5;
        };
    }

    private boolean containsIgnoreCase(String value, String keywordLowerCase) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keywordLowerCase);
    }


    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

}

