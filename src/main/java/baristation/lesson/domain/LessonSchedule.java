package baristation.lesson.domain;

import baristation.common.domain.BaseTimeEntity;
import baristation.lesson.enums.ScheduleStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "lesson_schedules")
public class LessonSchedule extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lesson_schedule_id")
    private Long lessonScheduleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(name = "lesson_date", nullable = false)
    private LocalDateTime lessonDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "duration")
    private Integer duration;

    @Column(name = "price", nullable = false)
    private Integer price;

    @Column(name = "reserved_count", nullable = false)
    @Builder.Default
    private Integer reservedCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_status", nullable = false, length = 20)
    @Builder.Default
    private ScheduleStatus scheduleStatus = ScheduleStatus.OPEN;
}