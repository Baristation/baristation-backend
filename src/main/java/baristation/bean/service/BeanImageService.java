package baristation.bean.service;

import baristation.bean.payload.response.BeanImageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface BeanImageService {
    // ==========================================
    // 2. 원두 이미지 관리 (Bean Image Management)
    // ==========================================

    /**
     * 상품의 대표(썸네일) 이미지를 업로드하거나 기존 이미지를 새 이미지로 교체합니다.
     */
    BeanImageResponse uploadThumb(Long productId, MultipartFile file) throws IOException;

    /**
     * 상품의 서브 이미지를 새롭게 추가합니다. (가장 마지막 순서로 배정)
     */
    BeanImageResponse uploadSub(Long productId, MultipartFile file) throws IOException;

    /**
     * 기존에 등록된 서브 이미지를 다른 이미지 파일로 교체합니다.
     */
    BeanImageResponse updateImage(Long beanImageId, MultipartFile file) throws IOException;

    /**
     * 상품 이미지를 삭제합니다. 서브 이미지일 경우 남은 이미지들의 정렬 순서를 재조정합니다.
     */
    void deleteImage(Long beanImageId);

    /**
     * 특정 상품에 등록된 모든 이미지 목록을 정렬 순서(SortOrder)에 맞춰 조회합니다.
     */
    List<BeanImageResponse> getImages(Long productId);
}
