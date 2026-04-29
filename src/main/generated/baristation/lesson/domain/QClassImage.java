package baristation.lesson.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QClassImage is a Querydsl query type for ClassImage
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QClassImage extends EntityPathBase<ClassImage> {

    private static final long serialVersionUID = 1473279303L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QClassImage classImage = new QClassImage("classImage");

    public final QLesson aClass;

    public final NumberPath<Long> classImageId = createNumber("classImageId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath imageUrl = createString("imageUrl");

    public final NumberPath<Integer> sortOrder = createNumber("sortOrder", Integer.class);

    public QClassImage(String variable) {
        this(ClassImage.class, forVariable(variable), INITS);
    }

    public QClassImage(Path<? extends ClassImage> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QClassImage(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QClassImage(PathMetadata metadata, PathInits inits) {
        this(ClassImage.class, metadata, inits);
    }

    public QClassImage(Class<? extends ClassImage> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.aClass = inits.isInitialized("aClass") ? new QLesson(forProperty("aClass"), inits.get("aClass")) : null;
    }

}

