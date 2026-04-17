package dripnote.home.payload.dto;

import dripnote.bean.domain.TastingNote;

public record HomeTastingsDTO(
        String tastingName,
        String tastingLink
) {
    // Entity → DTO 변환: 정적 팩토리 메서드 사용
    public static HomeTastingsDTO from(TastingNote tastingNote, Long tastingNoteId) {
        return new HomeTastingsDTO(
                tastingNote.getNameKo(),
                "/bean?tastingId=" + tastingNoteId
        );
    }
}