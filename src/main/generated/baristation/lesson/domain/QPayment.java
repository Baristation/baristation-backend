package baristation.lesson.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPayment is a Querydsl query type for Payment
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPayment extends EntityPathBase<Payment> {

    private static final long serialVersionUID = -1338071550L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPayment payment = new QPayment("payment");

    public final NumberPath<Integer> amount = createNumber("amount", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> approvedAt = createDateTime("approvedAt", java.time.LocalDateTime.class);

    public final QBooking booking;

    public final NumberPath<Integer> cancelAmount = createNumber("cancelAmount", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> cancelledAt = createDateTime("cancelledAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath currency = createString("currency");

    public final StringPath failureCode = createString("failureCode");

    public final StringPath failureMessage = createString("failureMessage");

    public final StringPath merchantOrderId = createString("merchantOrderId");

    public final StringPath method = createString("method");

    public final NumberPath<Long> paymentId = createNumber("paymentId", Long.class);

    public final StringPath paymentKey = createString("paymentKey");

    public final EnumPath<baristation.lesson.enums.PaymentStatus> paymentStatus = createEnum("paymentStatus", baristation.lesson.enums.PaymentStatus.class);

    public final EnumPath<baristation.lesson.enums.PaymentProvider> provider = createEnum("provider", baristation.lesson.enums.PaymentProvider.class);

    public final StringPath rawResponseJson = createString("rawResponseJson");

    public final DateTimePath<java.time.LocalDateTime> requestedAt = createDateTime("requestedAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QPayment(String variable) {
        this(Payment.class, forVariable(variable), INITS);
    }

    public QPayment(Path<? extends Payment> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPayment(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPayment(PathMetadata metadata, PathInits inits) {
        this(Payment.class, metadata, inits);
    }

    public QPayment(Class<? extends Payment> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.booking = inits.isInitialized("booking") ? new QBooking(forProperty("booking"), inits.get("booking")) : null;
    }

}

