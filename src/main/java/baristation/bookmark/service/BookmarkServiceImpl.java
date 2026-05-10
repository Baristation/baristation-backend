package baristation.bookmark.service;

import baristation.bean.domain.Product;
import baristation.bean.domain.ProductBookmark;
import baristation.bookmark.repository.ProductBookmarkRepository;
import baristation.bean.repository.ProductRepository;
import baristation.common.logging.TraceIdUtil;
import baristation.common.exception.CustomException;
import baristation.common.exception.ErrorCode;
import baristation.user.domain.User;
import baristation.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookmarkServiceImpl implements BookmarkService {

    private final ProductBookmarkRepository productBookmarkRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void toggleBookmark(Long productId, Long userId) {
        User userProxy = userRepository.getReferenceById(userId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.BEAN_NOT_FOUND));

        // 이미 존재하면 삭제
        productBookmarkRepository.findByUserAndProduct(userProxy, product)
                .ifPresentOrElse(
                        bookmark -> {
                            productBookmarkRepository.delete(bookmark);
                            log.info("[Bookmark] removed. userId={}, productId={}, traceId={}",
                                    userId, productId, TraceIdUtil.getTraceId());
                        },
                        () -> {
                            ProductBookmark newBookmark = ProductBookmark
                                    .builder()
                                    .user(userProxy)
                                    .product(product)
                                    .build();
                            productBookmarkRepository.save(newBookmark);
                            log.info("[Bookmark] created. userId={}, productId={}, traceId={}",
                                    userId, productId, TraceIdUtil.getTraceId());
                        }
                );
    }

}

