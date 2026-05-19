package baristation.lesson.domain;

import baristation.common.domain.BaseTimeEntity;
import baristation.lesson.enums.DifficultyLevel;
import baristation.lesson.enums.LessonCategory;
import baristation.lesson.enums.Region;
import baristation.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "lessons")
public class Lesson extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lesson_id")
    private Long lessonId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_user_id", nullable = false)
    private User hostUser;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "subtitle", length = 255)
    private String subtitle;

    @Enumerated(EnumType.STRING)
    @Column(name = "lesson_category")
    private LessonCategory lessonCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level", nullable = false, length = 20)
    private DifficultyLevel difficultyLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "region", length = 100)
    private Region region;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "place", length = 150)
    private String place;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "latitude", precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude;
}