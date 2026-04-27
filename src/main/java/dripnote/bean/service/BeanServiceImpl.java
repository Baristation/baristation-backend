package dripnote.bean.service;

import dripnote.bean.domain.Bean;
import dripnote.bean.domain.ProductImage;
import dripnote.bean.domain.ProductFlavorNote;
import dripnote.bean.enums.ImageType;
import dripnote.bean.payload.dto.ProductListItemDTO;
import dripnote.bean.payload.request.BeanSearchRequest;
import dripnote.bean.repository.ProductImageRepository;
import dripnote.bean.repository.ProductFlavorNoteRepository;
import dripnote.bean.repository.BeanRepository;
import dripnote.common.payload.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * BeanService 구현체
 * 원두 검색 및 목록 조회 기능을 담당
 *
 * - ERD 갱신 후 QueryDSL을 활용하여 동적 쿼리로 구현할 예정
 * - 현재는 간단한 필터링 로직으로 구현되어 있으며, 향후 최적화 필요
 */

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BeanServiceImpl implements BeanService {

    private final BeanRepository beanRepository;
    private final ProductFlavorNoteRepository productFlavorNoteRepository;
    private final ProductImageRepository productImageRepository;

    /**
     * 조건에 맞는 원두 목록을 검색하고 페이지네이션하여 반환
     * 
     * @param request 검색 필터 조건 (키워드, 맛, 로스팅 정도 등)
     * @param pageable 페이지네이션 정보
     * @return 페이지네이션된 BeanListItemDTO 응답
     */
    @Override
    public PageResponse<ProductListItemDTO> searchBeans(BeanSearchRequest request, Pageable pageable) {
        // 1. 필터 조건에 맞는 원두 조회
        List<Bean> beans = filterBeans();
        
        // 2. 페이지네이션 적용
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), beans.size());
        List<Bean> pagedBeans = beans.subList(start, end);
        
        // 3. DTO 변환 (대량 조회를 위해 관련 데이터를 한 번에 로드)
        List<ProductListItemDTO> dtos = convertToDTO(pagedBeans);
        
        // 4. Page 객체 생성 후 PageResponse로 변환
        Page<ProductListItemDTO> page = new PageImpl<>(dtos, pageable, beans.size());
        return PageResponse.of(page);
    }

    @Override
    public ProductListItemDTO getBeanDetail(Long beanId) {
        // TODO: 원두 상세 조회 구현 (BeanDetailDTO로 변환하여 반환)w
        return null;
    }

    /**
     * 검색 조건에 맞는 원두 목록을 필터링 (현재는 간단한 구현, ERD 갱신 후 QueryDSL 적용 가능)
     */
    private List<Bean> filterBeans() {
        // TODO: ERD 갱신 후 QueryDSL을 사용하여 동적 쿼리로 구현
        // 현재는 모든 원두를 조회
        return beanRepository.findAll();
    }

    /**
     * Bean 엔티티 리스트를 BeanListItemDTO로 변환
     * N+1 문제를 피하기 위해 대량 조회로 최적화
     */
    private List<ProductListItemDTO> convertToDTO(List<Bean> beans) {
        if (beans.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> beanIds = beans.stream()
                .map(Bean::getBeanId)
                .toList();

        // 1. 원두별 대표 이미지 조회 (ImageType.THUMB만 조회)
        Map<Long, String> imageUrlMap = getBeanImages(beanIds);

        // 2. 원두별 맛 노트(Tasting Note) 조회
        Map<Long, List<String>> tastingNotesMap = getBeanTastingNotes(beanIds);

        // 3. 원두를 DTO로 변환
        return beans.stream()
                .map(bean -> ProductListItemDTO.of(
                        bean,
                        bean.getRoaster().getNameKo(),
                        null,
                        imageUrlMap.get(bean.getBeanId())
                ))
                .toList();
    }

    /**
     * 원두별 대표 이미지 URL 맵 조회
     */
    private Map<Long, String> getBeanImages(List<Long> beanIds) {
        List<ProductImage> images = productImageRepository.findByProduct_ProductIdInAndImageType(
                beanIds,
                ImageType.THUMB
        );

        return images.stream()
                .collect(Collectors.toMap(
                        img -> img.getBean().getBeanId(),
                        ProductImage::getImageUrl,
                        (first, second) -> first // 중복 시 첫 번째 선택
                ));
    }

    /**
     * 원두별 맛 노트 리스트 맵 조회
     */
    private Map<Long, List<String>> getBeanTastingNotes(List<Long> beanIds) {
        List<ProductFlavorNote> productFlavorNotes = productFlavorNoteRepository.findByProduct_ProductIdIn(beanIds);

        return productFlavorNotes.stream()
                .collect(Collectors.groupingBy(
                        btn -> btn.getBean().getBeanId(),
                        Collectors.mapping(
                                btn -> btn.getTastingNote().getNameKo(),
                                Collectors.toList()
                        )
                ));
    }
}

