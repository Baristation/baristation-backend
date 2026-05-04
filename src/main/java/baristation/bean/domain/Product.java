package baristation.bean.domain;

import baristation.bean.enums.RoastingType;
import baristation.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "product")
public class Product extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roaster_id", nullable = false)
    private Roaster roaster;

    @Column(name = "name_ko", nullable = false, length = 150)
    private String nameKo;

    @Column(name = "name_en", length = 150)
    private String nameEn;

    @Enumerated(EnumType.STRING)
    @Column(name = "roasting_level")
    private RoastingType roastLevel;

    @Column(name = "agtron_min")
    private Integer agtronMin;

    @Column(name = "agtron_max")
    private Integer agtronMax;

    @Column(name = "acidity")
    private Integer acidity;

    @Column(name = "sweetness")
    private Integer sweetness;

    @Column(name = "body")
    private Integer body;

    @Column(name = "balance")
    private Integer balance;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "product_url", columnDefinition = "TEXT")
    private String productUrl;
}
