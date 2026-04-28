package baristation.bean.repository;

import baristation.bean.domain.Bean;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BeanRepository extends JpaRepository<Bean, Long> {

}
