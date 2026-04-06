package dripnote.bean.service;

import dripnote.bean.domain.Bean;
import dripnote.bean.domain.BeanImage;
import dripnote.bean.enums.ImageType;
import dripnote.bean.payload.response.BeanImageResponse;
import dripnote.bean.repository.BeanImagesRepository;
import dripnote.bean.repository.BeansRepository;
import dripnote.common.exception.CustomException;
import dripnote.common.service.R2ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static dripnote.common.exception.ErrorCode.*;

@Service
@Transactional
@RequiredArgsConstructor
public class BeanImageService {

    private final BeansRepository beansRepository;
    private final BeanImagesRepository beanImagesRepository;
    private final R2ImageService r2ImageService;

    private static final int THUMB_SORT_ORDER = 0;

    // 대표 이미지 업로드 또는 교체
    public BeanImageResponse uploadThumb(Long beanId, MultipartFile file) throws IOException {
        Bean bean = getBean(beanId);

        BeanImage thumbImage = beanImagesRepository.findByBean_BeanIdAndImageType(beanId, ImageType.THUMB)
                .orElse(null);

        // 썸네일(Thumb)이 없으면 이미지 저장
        if (thumbImage == null) {
            String imageUrl = r2ImageService.uploadBeanThumb(file, beanId);
            BeanImage saved = beanImagesRepository.save(
                    new BeanImage(bean, imageUrl, ImageType.THUMB, THUMB_SORT_ORDER)
            );
            return BeanImageResponse.from(saved);
        }

        // 썸네일(Thumb)이 있으면 이미지 수정
        String imageUrl = r2ImageService.updateByUrl(file, thumbImage.getImageUrl());
        thumbImage.changeImageUrl(imageUrl);
        thumbImage.changeSortOrder(0);

        return BeanImageResponse.from(thumbImage);
    }

    // 서브 이미지 추가
    public BeanImageResponse uploadSub(Long beanId, MultipartFile file) throws IOException {
        Bean bean = getBean(beanId);

        int nextSortOrder = beanImagesRepository.findMaxSubSortOrder(beanId) + 1;
        String imageUrl = r2ImageService.uploadBeanSubImage(file, beanId);

        BeanImage subImage = new BeanImage(bean, imageUrl, ImageType.SUB, nextSortOrder);
        BeanImage saved = beanImagesRepository.save(subImage);

        return BeanImageResponse.from(saved);
    }

    // 서브 이미지 교체
    public BeanImageResponse updateImage(Long beanImageId, MultipartFile file) throws IOException {
        BeanImage beanImage = beanImagesRepository.findById(beanImageId)
                .orElseThrow(() -> new CustomException(BEAN_IMAGE_NOT_FOUND));

        // 썸네일(Thumb) 수정은 uploadTumb()을 통해 수정
        if (beanImage.getImageType() == ImageType.THUMB) {
            throw new CustomException(THUMB_IMAGE_UPDATE_NOT_ALLOWED);
        }

        String imageUrl = r2ImageService.updateByUrl(file, beanImage.getImageUrl());
        beanImage.changeImageUrl(imageUrl);

        return BeanImageResponse.from(beanImage);
    }

    // 이미지 삭제
    public void deleteImage(Long beanImageId) {
        BeanImage beanImage = beanImagesRepository.findById(beanImageId)
                .orElseThrow(() -> new CustomException(BEAN_IMAGE_NOT_FOUND));

        Long beanId = beanImage.getBean().getBeanId();
        boolean isSubImage = beanImage.getImageType() == ImageType.SUB;

        r2ImageService.deleteByUrl(beanImage.getImageUrl());
        beanImagesRepository.delete(beanImage);

        if (isSubImage) {
            normalizeSubSortOrder(beanId);
        }
    }

    // 원두 이미지 조회
    @Transactional(readOnly = true)
    public List<BeanImageResponse> getImages(Long beanId) {
        getBean(beanId);

        return beanImagesRepository.findByBean_BeanIdOrderBySortOrderAsc(beanId)
                .stream()
                .map(BeanImageResponse::from)
                .toList();
    }


    // 원두가 존재하는지 확인
    private Bean getBean(Long beanId) {
        return beansRepository.findById(beanId)
                .orElseThrow(() -> new CustomException(BEAN_IMAGE_NOT_FOUND));
    }

    // 서브 이미지들을 다시 정렬
    private void normalizeSubSortOrder(Long beanId) {
        List<BeanImage> subImages = beanImagesRepository
                .findByBean_BeanIdAndImageTypeOrderBySortOrderAsc(beanId, ImageType.SUB);

        for (int i = 0; i < subImages.size(); i++) {
            subImages.get(i).changeSortOrder(i + 1);
        }
    }
}