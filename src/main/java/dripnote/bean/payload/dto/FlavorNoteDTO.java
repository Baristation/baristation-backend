package dripnote.bean.payload.dto;

import dripnote.bean.domain.FlavorNote;
import dripnote.bean.enums.FlavorCategory;

public record FlavorNoteDTO(
        Long flavorNoteId,
        FlavorCategory flavorCategory,
        String nameKo,
        String nameEn,
        String flavorImageUrl
) {
    public static FlavorNoteDTO from(FlavorNote flavorNote) {
        return new FlavorNoteDTO(
                flavorNote.getFlavorNoteId(),
                flavorNote.getFlavorCategory(),
                flavorNote.getNameKo(),
                flavorNote.getNameEn(),
                flavorNote.getFlavorImageUrl()
        );
    }
}