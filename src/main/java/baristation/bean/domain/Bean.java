package baristation.bean.domain;

import baristation.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "bean")
public class Bean extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bean_id")
    private Long beanId;

    @Column(name = "name_ko", nullable = false, length = 150)
    private String nameKo;

    @Column(name = "name_en", length = 150)
    private String nameEn;

    @Column(name = "process", length = 100)
    private String process;

    @Column(name = "origin", length = 100)
    private String origin;

    @Column(name = "region", length = 100)
    private String region;

    @Column(name = "variety", length = 100)
    private String variety;

    @Column(name = "altitude_min")
    private Integer altitudeMin;

    @Column(name = "altitude_max")
    private Integer altitudeMax;
}