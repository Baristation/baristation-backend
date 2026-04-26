package dripnote.bean.payload.dto;

import dripnote.bean.domain.Roaster;

public record RoasterDTO(
        Long roasterId,
        String nameKo,
        String nameEn,
        String homepageUrl,
        String description
) {
    public static RoasterDTO from(Roaster roaster) {
        return new RoasterDTO(
                roaster.getRoasterId(),
                roaster.getNameKo(),
                roaster.getNameEn(),
                roaster.getHomepageUrl(),
                roaster.getDescription()
        );
    }
}