package baristation.lesson.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QClassReview is a Querydsl query type for ClassReview
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QClassReview extends EntityPathBase<ClassReview> {

    private static final long serialVersionUID = -1322080020L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QClassReview classReview = new QClassReview("classReview");

    public final QBooking booking;

    public final NumberPath<Long> classReviewId = createNumber("classReviewId", Long.class);

    public final StringPath content = createString("content");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> rating = createNumber("rating", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QClassReview(String variable) {
        this(ClassReview.class, forVariable(variable), INITS);
    }

    public QClassReview(Path<? extends ClassReview> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QClassReview(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QClassReview(PathMetadata metadata, PathInits inits) {
        this(ClassReview.class, metadata, inits);
    }

    public QClassReview(Class<? extends ClassReview> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.booking = inits.isInitialized("booking") ? new QBooking(forProperty("booking"), inits.get("booking")) : null;
    }

}

