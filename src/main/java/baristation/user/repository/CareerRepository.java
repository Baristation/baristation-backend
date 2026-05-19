package baristation.user.repository;

import baristation.user.domain.Career;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CareerRepository extends JpaRepository<Career, Long> {
    List<Career> findByUser_UserId(Long userId);

}
