package baristation.bean.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QBean is a Querydsl query type for Bean
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBean extends EntityPathBase<Bean> {

    private static final long serialVersionUID = -620183636L;

    public static final QBean bean = new QBean("bean");

    public final baristation.common.domain.QBaseTimeEntity _super = new baristation.common.domain.QBaseTimeEntity(this);

    public final NumberPath<Long> beanId = createNumber("beanId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath nameEn = createString("nameEn");

    public final StringPath nameKo = createString("nameKo");

    public final StringPath origin = createString("origin");

    public final StringPath process = createString("process");

    public final StringPath region = createString("region");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QBean(String variable) {
        super(Bean.class, forVariable(variable));
    }

    public QBean(Path<? extends Bean> path) {
        super(path.getType(), path.getMetadata());
    }

    public QBean(PathMetadata metadata) {
        super(Bean.class, metadata);
    }

}

