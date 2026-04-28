package baristation.lesson.repository;

import baristation.lesson.domain.ClassImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassImageRepository extends JpaRepository<ClassImage, Long> {
}
