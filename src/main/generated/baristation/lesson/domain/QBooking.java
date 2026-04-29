package baristation.lesson.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBooking is a Querydsl query type for Booking
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBooking extends EntityPathBase<Booking> {

    private static final long serialVersionUID = -486704043L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QBooking booking = new QBooking("booking");

    public final StringPath attendeeName = createString("attendeeName");

    public final StringPath attendeePhone = createString("attendeePhone");

    public final NumberPath<Integer> bookedPrice = createNumber("bookedPrice", Integer.class);

    public final NumberPath<Long> bookingId = createNumber("bookingId", Long.class);

    public final EnumPath<baristation.lesson.enums.BookingStatus> bookingStatus = createEnum("bookingStatus", baristation.lesson.enums.BookingStatus.class);

    public final QClassSchedule classSchedule;

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath idempotencyKey = createString("idempotencyKey");

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final baristation.user.domain.QUser user;

    public QBooking(String variable) {
        this(Booking.class, forVariable(variable), INITS);
    }

    public QBooking(Path<? extends Booking> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QBooking(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QBooking(PathMetadata metadata, PathInits inits) {
        this(Booking.class, metadata, inits);
    }

    public QBooking(Class<? extends Booking> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.classSchedule = inits.isInitialized("classSchedule") ? new QClassSchedule(forProperty("classSchedule"), inits.get("classSchedule")) : null;
        this.user = inits.isInitialized("user") ? new baristation.user.domain.QUser(forProperty("user")) : null;
    }

}

