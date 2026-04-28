package baristation.bean.payload.dto;

import baristation.bean.domain.FlavorNote;

public record HomeFlavorDTO(
        String tastingName,
        String tastingLink
) {
    // Entity → DTO 변환: 정적 팩토리 메서드 사용
    public static HomeFlavorDTO from(FlavorNote flavorNote, Long flavorNoteId) {
        return new HomeFlavorDTO(
                flavorNote.getNameKo(),
                "/bean?flavorId=" + flavorNoteId
        );
    }
}