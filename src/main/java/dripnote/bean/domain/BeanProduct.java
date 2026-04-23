package dripnote.bean.domain;

import dripnote.common.domain.BaseTimeEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "bean_product")
public class BeanProduct extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bean_product_id")
    private Long beanProductId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bean_id", nullable = false)
    private Bean bean;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}
