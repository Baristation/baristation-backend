package baristation.bean.payload.dto;

import baristation.bean.domain.FlavorNote;
import baristation.bean.enums.FlavorCategory;

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