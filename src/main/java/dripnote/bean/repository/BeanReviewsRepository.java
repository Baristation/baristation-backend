package dripnote.bean.repository;

import dripnote.bean.domain.ProductReview;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BeanReviewsRepository extends JpaRepository<ProductReview, Long> {
}
