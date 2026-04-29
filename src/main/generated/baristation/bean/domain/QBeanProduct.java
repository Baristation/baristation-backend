package baristation.bean.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBeanProduct is a Querydsl query type for BeanProduct
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBeanProduct extends EntityPathBase<BeanProduct> {

    private static final long serialVersionUID = 1216539555L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QBeanProduct beanProduct = new QBeanProduct("beanProduct");

    public final baristation.common.domain.QBaseTimeEntity _super = new baristation.common.domain.QBaseTimeEntity(this);

    public final QBean bean;

    public final NumberPath<Long> beanProductId = createNumber("beanProductId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final QProduct product;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QBeanProduct(String variable) {
        this(BeanProduct.class, forVariable(variable), INITS);
    }

    public QBeanProduct(Path<? extends BeanProduct> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QBeanProduct(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QBeanProduct(PathMetadata metadata, PathInits inits) {
        this(BeanProduct.class, metadata, inits);
    }

    public QBeanProduct(Class<? extends BeanProduct> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.bean = inits.isInitialized("bean") ? new QBean(forProperty("bean")) : null;
        this.product = inits.isInitialized("product") ? new QProduct(forProperty("product"), inits.get("product")) : null;
    }

}

