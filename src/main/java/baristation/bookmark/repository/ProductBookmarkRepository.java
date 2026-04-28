package baristation.bookmark.repository;

import baristation.bean.domain.ProductBookmark;
import baristation.bean.domain.Product;
import baristation.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductBookmarkRepository extends JpaRepository<ProductBookmark, Long> {
    Optional<ProductBookmark> findByUserAndProduct(User user, Product product);
    boolean existsByUserAndProduct(User user, Product product);
    void deleteByUserAndProduct(User user, Product product);
}
