package baristation.home.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import baristation.bean.domain.FlavorNote;

public record HomeFlavorResponse(
        @JsonProperty("flavor_id")
        Long flavorId,

        @JsonProperty("flavor_name")
        String flavorName,

        @JsonProperty("flavor_image_url")
        String flavorImageUrl,

        @JsonProperty("flavor_link")
        String flavorLink
) {
    public static HomeFlavorResponse of(FlavorNote flavorNote, String flavorImageUrl) {
        return new HomeFlavorResponse(
                flavorNote.getFlavorNoteId(),
                flavorNote.getNameKo(),
                flavorImageUrl,
                "/api/products?flavorId=" + flavorNote.getFlavorNoteId()
        );
    }
}
