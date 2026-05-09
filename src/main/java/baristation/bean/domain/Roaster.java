package baristation.bean.domain;

import baristation.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "roasters")
public class Roaster extends BaseTimeEntity {
    /**
     * city, country, updatedAt -> 해당 데이터는 현재 불필요할 것 같아서 삭제했습니다!
     */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "roaster_id")
    private Long roasterId;

    @Column(name = "name_ko", nullable = false, length = 100)
    private String nameKo;

    @Column(name = "name_en", length = 100)
    private String nameEn;

    @Column(name = "homepage_url", length = 500)
    private String homepageUrl;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}