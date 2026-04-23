package dripnote.bean.repository;

import dripnote.bean.domain.FlavorNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlavorNoteRepository extends JpaRepository<FlavorNote, Long> {
    // ID 기준으로 오름차순 정렬 후 4개 반환
    List<FlavorNote> findTop4ByOrderByTastingNoteIdAsc();
}
