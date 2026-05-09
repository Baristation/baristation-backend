package baristation.bean.domain;

import baristation.bean.enums.FlavorCategory;
import baristation.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "flavor_note")
public class FlavorNote extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "flavor_note_id")
    private Long flavorNoteId;

    @Enumerated(EnumType.STRING)
    @Column(name = "flavor_category")
    private FlavorCategory flavorCategory;

    @Column(name = "name_ko", nullable = false, length = 50, unique = true)
    private String nameKo;

    @Column(name = "name_en", length = 50)
    private String nameEn;

    @Column(name = "flavor_image_url", length = 100)
    private String flavorImageUrl;
}