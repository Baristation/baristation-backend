package baristation.bean.service;

import baristation.bean.domain.Bean;
import baristation.bean.domain.BeanProduct;
import baristation.bean.domain.FlavorNote;
import baristation.bean.domain.Product;
import baristation.bean.domain.ProductFlavorNote;
import baristation.bean.domain.ProductImage;
import baristation.bean.domain.Roaster;
import baristation.bean.enums.BeanSortType;
import baristation.bean.enums.FlavorCategory;
import baristation.bean.enums.ImageType;
import baristation.bean.enums.RoastingType;
import baristation.bean.payload.dto.ProductDetailDTO;
import baristation.bean.payload.dto.ProductSummaryDTO;
import baristation.bean.payload.request.ProductSearchRequest;
import baristation.bean.repository.BeanProductRepository;
import baristation.bean.repository.ProductFlavorNoteRepository;
import baristation.bean.repository.ProductImageRepository;
import baristation.common.exception.CustomException;
import baristation.common.exception.ErrorCode;
import baristation.common.payload.response.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BeanServiceImplTest {

    @Mock
    private BeanProductRepository beanProductRepository;

    @Mock
    private ProductFlavorNoteRepository productFlavorNoteRepository;

    @Mock
    private ProductImageRepository productImageRepository;

    @InjectMocks
    private BeanServiceImpl beanService;

    @Test
    @DisplayName("원두 목록 조회: DB 없이 mock 엔티티로 페이지/썸네일 매핑을 검증한다")
    void searchProducts_withMockEntities_returnsPagedSummary() {
        Pageable pageable = PageRequest.of(0, 12);
        ProductSearchRequest request = new ProductSearchRequest(
                "ethiopia", FlavorCategory.CARAMELLY,
                2, 4,
                4, 4,
                1, 1,
                5, RoastingType.LIGHT, BeanSortType.ACIDITY
        );

        Bean bean1 = bean("Ethiopia Guji", "Ethiopia Guji", "Washed", "Ethiopia", "Guji");
        Bean bean2 = bean("Kenya AA", "Kenya AA", "Natural", "Kenya", "Nyeri");

        Product product1 = product(101L, RoastingType.LIGHT, 4, 3, 2, 3);
        Product product2 = product(102L, RoastingType.MEDIUM, 3, 4, 3, 4);

        // mock으로 대체 -> 원두와 상품 가져옴
        BeanProduct beanProduct1 = mock(BeanProduct.class);
        when(beanProduct1.getBean()).thenReturn(bean1);
        when(beanProduct1.getProduct()).thenReturn(product1);

        // 원두와 상품 2
        BeanProduct beanProduct2 = mock(BeanProduct.class);
        when(beanProduct2.getBean()).thenReturn(bean2);
        when(beanProduct2.getProduct()).thenReturn(product2);

        Page<BeanProduct> page = new PageImpl<>(List.of(beanProduct1, beanProduct2), pageable, 2);
        when(beanProductRepository.searchBeansWithFilters(eq(request), eq(pageable))).thenReturn(page);

        ProductImage thumb = ProductImage.builder()
                .productImageId(9001L)
                .product(product1)
                .imageType(ImageType.THUMB)
                .imageUrl("https://cdn.example.com/thumb-101.jpg")
                .sortOrder(0)
                .build();

        when(productImageRepository.findByProduct_ProductIdInAndImageType(eq(List.of(101L, 102L)), eq(ImageType.THUMB)))
                .thenReturn(List.of(thumb));

        PageResponse<ProductSummaryDTO> response = beanService.searchProducts(request, pageable);

        assertThat(response.totalElements()).isEqualTo(2);
        assertThat(response.content()).hasSize(2);
        assertThat(response.content().get(0).productId()).isEqualTo(101L);
        assertThat(response.content().get(0).productImage()).isNotNull();
        assertThat(response.content().get(0).productImage().imageType()).isEqualTo(ImageType.THUMB);
        assertThat(response.content().get(1).productId()).isEqualTo(102L);
        assertThat(response.content().get(1).productImage()).isNull();
    }

    @Test
    @DisplayName("원두 목록 조회: 최소값이 최대값보다 크면 600-2 예외를 던진다")
    void searchProducts_invalidRange_throwsCustomException() {
        ProductSearchRequest invalidRequest = new ProductSearchRequest(
                null, null,
                5, 3,
                null, null,
                null, null,
                null, null, null
        );

        assertThatThrownBy(() -> beanService.searchProducts(invalidRequest, PageRequest.of(0, 12)))
                .isInstanceOf(CustomException.class)
                .extracting(ex -> ((CustomException) ex).getErrorCode())
                .isEqualTo(ErrorCode.BEAN_SEARCH_INVALID_RANGE);
    }

    @Test
    @DisplayName("원두 상세 조회: mock 엔티티로 썸네일/상세이미지/향미노트 매핑을 검증한다")
    void getProductDetail_withMockEntities_returnsDetail() {
        Long productId = 300L;

        Bean bean = bean("콜롬비아 수프리모", "Colombia Supremo", "Washed", "Colombia", "Huila");
        Product product = product(productId, RoastingType.MEDIUMDARK, 2, 3, 4, 3);
        product.setDescription("초콜릿과 견과류 중심의 밸런스 좋은 컵");

        Roaster roaster = new Roaster();
        roaster.setRoasterId(77L);
        roaster.setNameKo("바리스테이션 로스터리");
        roaster.setNameEn("바리스테이션 Roastery");
        roaster.setHomepageUrl("https://dripnote.example.com");
        roaster.setDescription("테스트용 로스터리");
        product.setRoaster(roaster);

        BeanProduct selected = mock(BeanProduct.class);
        when(selected.getBean()).thenReturn(bean);
        when(selected.getProduct()).thenReturn(product);
        when(beanProductRepository.findByProductId(productId)).thenReturn(selected);

        ProductImage thumb = ProductImage.builder()
                .productImageId(1L)
                .product(product)
                .imageType(ImageType.THUMB)
                .imageUrl("https://cdn.example.com/thumb.jpg")
                .sortOrder(0)
                .build();

        ProductImage detail1 = ProductImage.builder()
                .productImageId(2L)
                .product(product)
                .imageType(ImageType.SUB)
                .imageUrl("https://cdn.example.com/detail-1.jpg")
                .sortOrder(1)
                .build();

        ProductImage detail2 = ProductImage.builder()
                .productImageId(3L)
                .product(product)
                .imageType(ImageType.SUB)
                .imageUrl("https://cdn.example.com/detail-2.jpg")
                .sortOrder(2)
                .build();

        when(productImageRepository.findByProduct_ProductIdOrderBySortOrderAsc(productId))
                .thenReturn(List.of(thumb, detail1, detail2));
        when(productImageRepository.findByProduct_ProductIdAndImageTypeOrderBySortOrderAsc(productId, ImageType.THUMB))
                .thenReturn(List.of(thumb));

        FlavorNote flavorNote = new FlavorNote();
        flavorNote.setFlavorNoteId(501L);
        flavorNote.setFlavorCategory(FlavorCategory.NUTTY);
        flavorNote.setNameKo("헤이즐넛");
        flavorNote.setNameEn("Hazelnut");

        ProductFlavorNote productFlavorNote = new ProductFlavorNote();
        productFlavorNote.setProduct(product);
        productFlavorNote.setFlavorNote(flavorNote);

        when(productFlavorNoteRepository.findByProduct_ProductIdIn(List.of(productId)))
                .thenReturn(List.of(productFlavorNote));

        ProductDetailDTO detail = beanService.getProductDetail(productId);

        assertThat(detail.beanSummary().productId()).isEqualTo(productId);
        assertThat(detail.beanSummary().beanNameKo()).isEqualTo("콜롬비아 수프리모");
        assertThat(detail.beanSummary().productImage()).isNotNull();
        assertThat(detail.beanSummary().productImage().imageType()).isEqualTo(ImageType.THUMB);

        assertThat(detail.images()).hasSize(2);
        assertThat(detail.images()).extracting(image -> image.imageType()).doesNotContain(ImageType.THUMB);

        assertThat(detail.flavorNotes()).hasSize(1);
        assertThat(detail.flavorNotes().get(0).nameKo()).isEqualTo("헤이즐넛");
        assertThat(detail.roaster().nameKo()).isEqualTo("드립노트 로스터리");
    }

    @Test
    @DisplayName("원두 상세 조회: 상품이 없으면 600-1 예외를 던진다")
    void getProductDetail_whenMissing_throwsCustomException() {
        when(beanProductRepository.findByProductId(any())).thenReturn(null);

        assertThatThrownBy(() -> beanService.getProductDetail(999L))
                .isInstanceOf(CustomException.class)
                .extracting(ex -> ((CustomException) ex).getErrorCode())
                .isEqualTo(ErrorCode.BEAN_NOT_FOUND);
    }

    private Bean bean(String nameKo, String nameEn, String process, String origin, String region) {
        Bean bean = new Bean();
        bean.setNameKo(nameKo);
        bean.setNameEn(nameEn);
        bean.setProcess(process);
        bean.setOrigin(origin);
        bean.setRegion(region);
        return bean;
    }

    private Product product(Long productId, RoastingType roastLevel, Integer acidity, Integer sweetness, Integer body, Integer balance) {
        Product product = new Product();
        product.setProductId(productId);
        product.setRoastLevel(roastLevel);
        product.setAcidity(acidity);
        product.setSweetness(sweetness);
        product.setBody(body);
        product.setBalance(balance);
        return product;
    }
}


