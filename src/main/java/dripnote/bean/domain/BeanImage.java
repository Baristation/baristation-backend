package dripnote.bean.domain;

import dripnote.bean.enums.ImageType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "bean_images")
public class BeanImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bean_image_id")
    private Long beanImageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bean_id", nullable = false)
    private Bean bean;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "image_type", length = 20)
    private ImageType imageType;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public void changeImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void changeSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}