package baristation.bean.repository;

import baristation.bean.domain.BeanProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BeanProductRepository extends JpaRepository<BeanProduct, Long>, BeanProductRepositoryCustom {
}
