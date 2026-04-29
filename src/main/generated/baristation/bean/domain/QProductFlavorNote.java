package baristation.bean.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QProductFlavorNote is a Querydsl query type for ProductFlavorNote
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QProductFlavorNote extends EntityPathBase<ProductFlavorNote> {

    private static final long serialVersionUID = -807274621L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QProductFlavorNote productFlavorNote = new QProductFlavorNote("productFlavorNote");

    public final baristation.common.domain.QBaseTimeEntity _super = new baristation.common.domain.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final QFlavorNote flavorNote;

    public final QProduct product;

    public final NumberPath<Long> productFlavorNoteId = createNumber("productFlavorNoteId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QProductFlavorNote(String variable) {
        this(ProductFlavorNote.class, forVariable(variable), INITS);
    }

    public QProductFlavorNote(Path<? extends ProductFlavorNote> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QProductFlavorNote(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QProductFlavorNote(PathMetadata metadata, PathInits inits) {
        this(ProductFlavorNote.class, metadata, inits);
    }

    public QProductFlavorNote(Class<? extends ProductFlavorNote> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.flavorNote = inits.isInitialized("flavorNote") ? new QFlavorNote(forProperty("flavorNote")) : null;
        this.product = inits.isInitialized("product") ? new QProduct(forProperty("product"), inits.get("product")) : null;
    }

}

