package baristation.bean.repository;

import baristation.bean.domain.FlavorNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlavorNoteRepository extends JpaRepository<FlavorNote, Long> {
    List<FlavorNote> findTop8ByOrderByFlavorNoteIdAsc();
}
