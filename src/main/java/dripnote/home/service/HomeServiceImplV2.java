package dripnote.home.service;

import dripnote.bean.repository.BeanImagesRepository;
import dripnote.bean.repository.BeanTastingNotesRepository;
import dripnote.bean.repository.BeansRepository;
import dripnote.bean.repository.TastingNoteRepository;
import dripnote.home.payload.dto.HomeBeanDTO;
import dripnote.home.payload.request.BeanSearchRequest;
import dripnote.home.payload.response.HomeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeServiceImplV2 implements HomeService{

    private final BeansRepository beansRepository;
    private final TastingNoteRepository tastingNoteRepository;
    private final BeanTastingNotesRepository beanTastingNotesRepository;
    private final BeanImagesRepository beanImagesRepository;


    @Override
    public HomeResponse getHome(BeanSearchRequest beanSearchRequest, Pageable pageable) {
        return null;
    }

    @Override
    public List<HomeBeanDTO> getBeans() {
        return List.of();
    }
}
