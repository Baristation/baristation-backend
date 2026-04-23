package dripnote.home.service;

import dripnote.bean.domain.*;
import dripnote.bean.enums.ImageType;
import dripnote.bean.repository.*;
import dripnote.bean.payload.dto.BeanListItemDTO;
import dripnote.home.payload.response.HomeResponse;
import dripnote.bean.payload.dto.HomeTastingsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HomeServiceImplV1 implements HomeService {

    private final ProductRepository productRepository;
    private final FlavorNoteRepository flavorNoteRepository;
    private final ProductFlavorNoteRepository productFlavorNoteRepository;
    private final ProductImageRepository productImageRepository;

    // 메인 페이지 향미, 원두 정보 전송
    public HomeResponse getHome() {
//        List<HomeTastingsDTO> tastings = getTastings();
        List<BeanListItemDTO> beans = getBeans();

        return HomeResponse.builder()
                .beans(beans)
                .build();
    }
//    // 향미 목록 조회
    public List<HomeTastingsDTO> getTastings() {
        List<FlavorNote> flavorNotes = flavorNoteRepository.findTop4ByOrderByTastingNoteIdAsc();

        return flavorNotes.stream()
                .map(flavorNote -> HomeTastingsDTO.from(flavorNote, flavorNote.getTastingNoteId()))
                .toList();
    }

    // 원두 목록 조회
    public List<BeanListItemDTO> getBeans() {
        List<Product> products = productRepository.findTop4ByOrderByCreatedAtDesc();

        if (beans.isEmpty()) {
            return List.of();
        }

        List<Long> beanIds = beans.stream()
                .map(Bean::getBeanId)
                .toList();

        List<ProductFlavorNote> productFlavorNotes =
                productFlavorNoteRepository.findByBean_BeanIdIn(beanIds);

        List<ProductImage> productImages =
                productImageRepository.findByBean_BeanIdInAndImageType(beanIds, ImageType.THUMB);

        Map<Long, List<String>> beanTastingMap = new LinkedHashMap<>();
        for (ProductFlavorNote productFlavorNote : productFlavorNotes) {
            Long beanId = productFlavorNote.getBean().getBeanId();
            String tastingName = productFlavorNote.getTastingNote().getNameKo();

            beanTastingMap
                    .computeIfAbsent(beanId, key -> new ArrayList<>())
                    .add(tastingName);
        }

        Map<Long, String> beanMainImageMap = new LinkedHashMap<>();
        for (ProductImage productImage : productImages) {
            Long beanId = productImage.getBean().getBeanId();

            beanMainImageMap.putIfAbsent(beanId, productImage.getImageUrl());
        }

        return beans.stream()
                .map(bean -> BeanListItemDTO.of(bean, beanTastingMap.getOrDefault(bean.getBeanId(), List.of()), beanMainImageMap.get(bean.getBeanId())))
                .toList();
    }
}
