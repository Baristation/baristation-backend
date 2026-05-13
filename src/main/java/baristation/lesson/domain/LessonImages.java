package baristation.lesson.domain;

import baristation.bean.enums.ImageType;
import baristation.common.domain.BaseTimeEntity;
import baristation.lesson.enums.LessonCategory;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "lesson_images")
public class LessonImages extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lesson_image_id")
    private Long lessonImageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "image_type")
    private ImageType imageType;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;
}