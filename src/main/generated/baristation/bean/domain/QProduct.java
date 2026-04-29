package baristation.bean.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QProduct is a Querydsl query type for Product
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QProduct extends EntityPathBase<Product> {

    private static final long serialVersionUID = 983682931L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QProduct product = new QProduct("product");

    public final baristation.common.domain.QBaseTimeEntity _super = new baristation.common.domain.QBaseTimeEntity(this);

    public final NumberPath<Integer> acidity = createNumber("acidity", Integer.class);

    public final NumberPath<Integer> agtronMax = createNumber("agtronMax", Integer.class);

    public final NumberPath<Integer> agtronMin = createNumber("agtronMin", Integer.class);

    public final NumberPath<Integer> balance = createNumber("balance", Integer.class);

    public final NumberPath<Integer> body = createNumber("body", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath description = createString("description");

    public final StringPath nameEn = createString("nameEn");

    public final StringPath nameKo = createString("nameKo");

    public final NumberPath<Long> productId = createNumber("productId", Long.class);

    public final QRoaster roaster;

    public final EnumPath<baristation.bean.enums.RoastingType> roastLevel = createEnum("roastLevel", baristation.bean.enums.RoastingType.class);

    public final NumberPath<Integer> sweetness = createNumber("sweetness", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QProduct(String variable) {
        this(Product.class, forVariable(variable), INITS);
    }

    public QProduct(Path<? extends Product> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QProduct(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QProduct(PathMetadata metadata, PathInits inits) {
        this(Product.class, metadata, inits);
    }

    public QProduct(Class<? extends Product> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.roaster = inits.isInitialized("roaster") ? new QRoaster(forProperty("roaster")) : null;
    }

}

