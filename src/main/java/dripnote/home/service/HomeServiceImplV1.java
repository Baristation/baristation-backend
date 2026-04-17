package dripnote.home.service;

import dripnote.bean.domain.Bean;
import dripnote.bean.domain.BeanImage;
import dripnote.bean.domain.BeanTastingNote;
import dripnote.bean.domain.TastingNote;
import dripnote.bean.enums.ImageType;
import dripnote.bean.repository.BeanImagesRepository;
import dripnote.bean.repository.BeanTastingNotesRepository;
import dripnote.bean.repository.BeansRepository;
import dripnote.bean.repository.TastingNoteRepository;
import dripnote.home.payload.dto.HomeBeanDTO;
import dripnote.home.payload.request.BeanSearchRequest;
import dripnote.home.payload.response.HomeResponse;
import dripnote.home.payload.dto.HomeTastingsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HomeServiceImplV1 implements HomeService {

    private final BeansRepository beansRepository;
    private final TastingNoteRepository tastingNoteRepository;
    private final BeanTastingNotesRepository beanTastingNotesRepository;
    private final BeanImagesRepository beanImagesRepository;

    // 메인 페이지 향미, 원두 정보 전송
    public HomeResponse getHome(BeanSearchRequest beanSearchRequest,
                                Pageable pageable) {
//        List<HomeTastingsDTO> tastings = getTastings();
        List<HomeBeanDTO> beans = getBeans();

        return HomeResponse.builder()
                .beans(beans)
                .build();
    }
//    // 향미 목록 조회
//    public List<HomeTastingsDTO> getTastings() {
//        List<TastingNote> tastingNotes = tastingNoteRepository.findTop4ByOrderByTastingNoteIdAsc();
//
//        return tastingNotes.stream()
//                .map(tastingNote -> HomeTastingsDTO.from(tastingNote, tastingNote.getTastingNoteId()))
//                .toList();
//    }

    // 원두 목록 조회
    public List<HomeBeanDTO> getBeans() {
        List<Bean> beans = beansRepository.findTop4ByOrderByCreatedAtDesc();

        if (beans.isEmpty()) {
            return List.of();
        }

        List<Long> beanIds = beans.stream()
                .map(Bean::getBeanId)
                .toList();

        List<BeanTastingNote> beanTastingNotes =
                beanTastingNotesRepository.findByBean_BeanIdIn(beanIds);

        List<BeanImage> beanImages =
                beanImagesRepository.findByBean_BeanIdInAndImageType(beanIds, ImageType.THUMB);

        Map<Long, List<String>> beanTastingMap = new LinkedHashMap<>();
        for (BeanTastingNote beanTastingNote : beanTastingNotes) {
            Long beanId = beanTastingNote.getBean().getBeanId();
            String tastingName = beanTastingNote.getTastingNote().getNameKo();

            beanTastingMap
                    .computeIfAbsent(beanId, key -> new ArrayList<>())
                    .add(tastingName);
        }

        Map<Long, String> beanMainImageMap = new LinkedHashMap<>();
        for (BeanImage beanImage : beanImages) {
            Long beanId = beanImage.getBean().getBeanId();

            beanMainImageMap.putIfAbsent(beanId, beanImage.getImageUrl());
        }

        return beans.stream()
                .map(bean -> HomeBeanDTO.of(bean, beanTastingMap.getOrDefault(bean.getBeanId(), List.of()), beanMainImageMap.get(bean.getBeanId())))
                .toList();
    }
}
