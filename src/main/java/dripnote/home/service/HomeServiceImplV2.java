package dripnote.home.service;

import dripnote.bean.repository.ProductImageRepository;
import dripnote.bean.repository.ProductFlavorNoteRepository;
import dripnote.bean.repository.BeanRepository;
import dripnote.bean.repository.FlavorNoteRepository;
import dripnote.common.payload.response.PageResponse;
import dripnote.bean.payload.dto.BeanListItemDTO;
import dripnote.bean.payload.request.BeanSearchRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeServiceImplV2 implements HomeService{

    private final BeanRepository beanRepository;
    private final FlavorNoteRepository flavorNoteRepository;
    private final ProductFlavorNoteRepository productFlavorNoteRepository;
    private final ProductImageRepository productImageRepository;


    @Override
    public PageResponse<BeanListItemDTO> getHome(BeanSearchRequest beanSearchRequest, Pageable pageable) {
        return null;
    }

    @Override
    public List<BeanListItemDTO> getBeans() {
        return List.of();
    }
}
