package baristation.lesson.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QLesson is a Querydsl query type for Lesson
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QLesson extends EntityPathBase<Lesson> {

    private static final long serialVersionUID = -15611460L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QLesson lesson = new QLesson("lesson");

    public final StringPath address = createString("address");

    public final NumberPath<Long> classId = createNumber("classId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath curriculum = createString("curriculum");

    public final StringPath description = createString("description");

    public final EnumPath<baristation.lesson.enums.DifficultyLevel> difficultyLevel = createEnum("difficultyLevel", baristation.lesson.enums.DifficultyLevel.class);

    public final NumberPath<Integer> durationMin = createNumber("durationMin", Integer.class);

    public final StringPath extraCostNote = createString("extraCostNote");

    public final baristation.user.domain.QUser hostUser;

    public final NumberPath<java.math.BigDecimal> latitude = createNumber("latitude", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> longitude = createNumber("longitude", java.math.BigDecimal.class);

    public final StringPath placeName = createString("placeName");

    public final StringPath refundPolicy = createString("refundPolicy");

    public final StringPath regionName = createString("regionName");

    public final StringPath subtitle = createString("subtitle");

    public final StringPath suppliesNote = createString("suppliesNote");

    public final StringPath title = createString("title");

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QLesson(String variable) {
        this(Lesson.class, forVariable(variable), INITS);
    }

    public QLesson(Path<? extends Lesson> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QLesson(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QLesson(PathMetadata metadata, PathInits inits) {
        this(Lesson.class, metadata, inits);
    }

    public QLesson(Class<? extends Lesson> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.hostUser = inits.isInitialized("hostUser") ? new baristation.user.domain.QUser(forProperty("hostUser")) : null;
    }

}

