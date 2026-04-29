package baristation.bean.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QProductBookmark is a Querydsl query type for ProductBookmark
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QProductBookmark extends EntityPathBase<ProductBookmark> {

    private static final long serialVersionUID = 1971500489L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QProductBookmark productBookmark = new QProductBookmark("productBookmark");

    public final baristation.common.domain.QBaseTimeEntity _super = new baristation.common.domain.QBaseTimeEntity(this);

    public final NumberPath<Long> bookmarkId = createNumber("bookmarkId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final QProduct product;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final baristation.user.domain.QUser user;

    public QProductBookmark(String variable) {
        this(ProductBookmark.class, forVariable(variable), INITS);
    }

    public QProductBookmark(Path<? extends ProductBookmark> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QProductBookmark(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QProductBookmark(PathMetadata metadata, PathInits inits) {
        this(ProductBookmark.class, metadata, inits);
    }

    public QProductBookmark(Class<? extends ProductBookmark> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.product = inits.isInitialized("product") ? new QProduct(forProperty("product"), inits.get("product")) : null;
        this.user = inits.isInitialized("user") ? new baristation.user.domain.QUser(forProperty("user")) : null;
    }

}

