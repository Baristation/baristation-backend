package dripnote.bean.repository;

import dripnote.bean.domain.ProductBookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BeanBookmarksRepository extends JpaRepository<ProductBookmark, Long> {
}
