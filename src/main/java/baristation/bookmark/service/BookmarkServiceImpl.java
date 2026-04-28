package baristation.bookmark.service;

import baristation.bean.domain.Product;
import baristation.bean.domain.ProductBookmark;
import baristation.bookmark.repository.ProductBookmarkRepository;
import baristation.bean.repository.ProductRepository;
import baristation.common.exception.CustomException;
import baristation.common.exception.ErrorCode;
import baristation.user.domain.User;
import baristation.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
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
                        bookmark -> productBookmarkRepository.delete(bookmark),
                        () -> {
                            ProductBookmark newBookmark = ProductBookmark
                                    .builder()
                                    .user(userProxy)
                                    .product(product)
                                    .build();
                            productBookmarkRepository.save(newBookmark);
                        }
                );
    }

}

