package baristation.bean.repository;

import baristation.bean.domain.Roaster;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoastersRepository extends JpaRepository<Roaster, Long> {
}
