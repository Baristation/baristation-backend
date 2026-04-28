package baristation.bean.repository;

import baristation.bean.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    // 생성일 기준으로 내림차순 정렬 후 4개 반환
    List<Product> findTop4ByOrderByCreatedAtDesc();
}
