package baristation.bean.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QRoaster is a Querydsl query type for Roaster
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRoaster extends EntityPathBase<Roaster> {

    private static final long serialVersionUID = -1634647786L;

    public static final QRoaster roaster = new QRoaster("roaster");

    public final baristation.common.domain.QBaseTimeEntity _super = new baristation.common.domain.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath description = createString("description");

    public final StringPath homepageUrl = createString("homepageUrl");

    public final StringPath nameEn = createString("nameEn");

    public final StringPath nameKo = createString("nameKo");

    public final NumberPath<Long> roasterId = createNumber("roasterId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QRoaster(String variable) {
        super(Roaster.class, forVariable(variable));
    }

    public QRoaster(Path<? extends Roaster> path) {
        super(path.getType(), path.getMetadata());
    }

    public QRoaster(PathMetadata metadata) {
        super(Roaster.class, metadata);
    }

}

