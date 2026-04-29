package baristation.bean.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QFlavorNote is a Querydsl query type for FlavorNote
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFlavorNote extends EntityPathBase<FlavorNote> {

    private static final long serialVersionUID = 268009132L;

    public static final QFlavorNote flavorNote = new QFlavorNote("flavorNote");

    public final baristation.common.domain.QBaseTimeEntity _super = new baristation.common.domain.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final EnumPath<baristation.bean.enums.FlavorCategory> flavorCategory = createEnum("flavorCategory", baristation.bean.enums.FlavorCategory.class);

    public final StringPath flavorImageUrl = createString("flavorImageUrl");

    public final NumberPath<Long> flavorNoteId = createNumber("flavorNoteId", Long.class);

    public final StringPath nameEn = createString("nameEn");

    public final StringPath nameKo = createString("nameKo");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QFlavorNote(String variable) {
        super(FlavorNote.class, forVariable(variable));
    }

    public QFlavorNote(Path<? extends FlavorNote> path) {
        super(path.getType(), path.getMetadata());
    }

    public QFlavorNote(PathMetadata metadata) {
        super(FlavorNote.class, metadata);
    }

}

