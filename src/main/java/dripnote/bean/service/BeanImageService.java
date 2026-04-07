package dripnote.bean.service;

import dripnote.bean.domain.Bean;
import dripnote.bean.domain.BeanImage;
import dripnote.bean.enums.ImageType;
import dripnote.bean.payload.response.BeanImageResponse;
import dripnote.bean.repository.BeanImagesRepository;
import dripnote.bean.repository.BeansRepository;
import dripnote.common.exception.CustomException;
import dripnote.common.r2.R2ImageService;
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

        // 대표 이미지가 없으면 새로 저장
        if (thumbImage == null) {
            String imageUrl = r2ImageService.uploadBeanThumb(file, beanId);

            BeanImage saved = beanImagesRepository.save(
                    BeanImage.builder()
                            .bean(bean)
                            .imageType(ImageType.THUMB)
                            .imageUrl(imageUrl)
                            .sortOrder(THUMB_SORT_ORDER)
                            .build()
            );

            return BeanImageResponse.from(saved);
        }

        // 대표 이미지가 있으면 "기존 URL 경로 재사용"이 아니라
        // 항상 새 규칙 경로로 업로드
        String oldImageUrl = thumbImage.getImageUrl();
        String newImageUrl = r2ImageService.uploadBeanThumb(file, beanId);

        // 예전 구버전 경로였다면 정리
        // 이미 새 경로였다면 uploadBeanThumb()가 같은 위치를 덮어썼을 수 있으므로 삭제하면 안 됨
        if (!oldImageUrl.equals(newImageUrl)) {
            r2ImageService.deleteByUrl(oldImageUrl);
        }

        thumbImage.changeImageUrl(newImageUrl);
        thumbImage.changeSortOrder(THUMB_SORT_ORDER);

        return BeanImageResponse.from(thumbImage);
    }

    // 서브 이미지 추가
    public BeanImageResponse uploadSub(Long beanId, MultipartFile file) throws IOException {
        Bean bean = getBean(beanId);

        int nextSortOrder = beanImagesRepository.findMaxSubSortOrder(beanId) + 1;
        String imageUrl = r2ImageService.uploadBeanSubImage(file, beanId);

        BeanImage subImage = BeanImage.builder()
                .bean(bean)
                .imageType(ImageType.SUB)
                .imageUrl(imageUrl)
                .sortOrder(nextSortOrder)
                .build();

        BeanImage saved = beanImagesRepository.save(subImage);

        return BeanImageResponse.from(saved);
    }

    // 서브 이미지 교체
    public BeanImageResponse updateImage(Long beanImageId, MultipartFile file) throws IOException {
        BeanImage beanImage = beanImagesRepository.findById(beanImageId)
                .orElseThrow(() -> new CustomException(BEAN_IMAGE_NOT_FOUND));

        // 대표 이미지는 uploadThumb()을 통해 수정
        if (beanImage.getImageType() == ImageType.THUMB) {
            throw new CustomException(THUMB_IMAGE_UPDATE_NOT_ALLOWED);
        }

        String oldImageUrl = beanImage.getImageUrl();
        Long beanId = beanImage.getBean().getBeanId();

        // 항상 새 규칙 경로로 업로드
        String newImageUrl = r2ImageService.uploadBeanSubImage(file, beanId);

        // 기존 파일 삭제
        r2ImageService.deleteByUrl(oldImageUrl);

        beanImage.changeImageUrl(newImageUrl);

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

    // 원두 존재 여부 확인
    private Bean getBean(Long beanId) {
        return beansRepository.findById(beanId)
                .orElseThrow(() -> new CustomException(BEAN_NOT_FOUND));
    }

    // 서브 이미지 정렬 재조정
    private void normalizeSubSortOrder(Long beanId) {
        List<BeanImage> subImages = beanImagesRepository
                .findByBean_BeanIdAndImageTypeOrderBySortOrderAsc(beanId, ImageType.SUB);

        for (int i = 0; i < subImages.size(); i++) {
            subImages.get(i).changeSortOrder(i + 1);
        }
    }
}