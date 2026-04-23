package dripnote.bean.payload.dto;

import dripnote.bean.domain.FlavorNote;

public record HomeTastingsDTO(
        String tastingName,
        String tastingLink
) {
    // Entity → DTO 변환: 정적 팩토리 메서드 사용
    public static HomeTastingsDTO from(FlavorNote flavorNote, Long tastingNoteId) {
        return new HomeTastingsDTO(
                flavorNote.getNameKo(),
                "/bean?tastingId=" + tastingNoteId
        );
    }
}