package baristation.lesson.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QClassSchedule is a Querydsl query type for ClassSchedule
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QClassSchedule extends EntityPathBase<ClassSchedule> {

    private static final long serialVersionUID = 354692907L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QClassSchedule classSchedule = new QClassSchedule("classSchedule");

    public final QLesson aClass;

    public final NumberPath<Integer> capacity = createNumber("capacity", Integer.class);

    public final DatePath<java.time.LocalDate> classDate = createDate("classDate", java.time.LocalDate.class);

    public final NumberPath<Long> classScheduleId = createNumber("classScheduleId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final TimePath<java.time.LocalTime> endTime = createTime("endTime", java.time.LocalTime.class);

    public final NumberPath<Integer> price = createNumber("price", Integer.class);

    public final NumberPath<Integer> reservedCount = createNumber("reservedCount", Integer.class);

    public final EnumPath<baristation.lesson.enums.ScheduleStatus> scheduleStatus = createEnum("scheduleStatus", baristation.lesson.enums.ScheduleStatus.class);

    public final TimePath<java.time.LocalTime> startTime = createTime("startTime", java.time.LocalTime.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QClassSchedule(String variable) {
        this(ClassSchedule.class, forVariable(variable), INITS);
    }

    public QClassSchedule(Path<? extends ClassSchedule> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QClassSchedule(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QClassSchedule(PathMetadata metadata, PathInits inits) {
        this(ClassSchedule.class, metadata, inits);
    }

    public QClassSchedule(Class<? extends ClassSchedule> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.aClass = inits.isInitialized("aClass") ? new QLesson(forProperty("aClass"), inits.get("aClass")) : null;
    }

}

