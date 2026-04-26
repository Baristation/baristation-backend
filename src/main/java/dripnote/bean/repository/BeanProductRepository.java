package dripnote.bean.repository;

import dripnote.bean.domain.BeanProduct;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BeanProductRepository extends JpaRepository<BeanProduct, Long> {
	@EntityGraph(attributePaths = {"bean", "product", "product.roaster"})
	@Query("SELECT bp FROM BeanProduct bp")
	List<BeanProduct> findAllWithBeanAndProductAndRoaster();
}
