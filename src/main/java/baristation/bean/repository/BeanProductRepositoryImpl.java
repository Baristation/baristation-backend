package baristation.bean.repository;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import baristation.bean.domain.BeanProduct;
import baristation.bean.enums.FlavorCategory;
import baristation.bean.enums.RoastingType;
import baristation.bean.payload.request.ProductSearchRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Repository;

import java.util.List;

import static baristation.bean.domain.QBean.bean;
import static baristation.bean.domain.QBeanProduct.beanProduct;
import static baristation.bean.domain.QFlavorNote.flavorNote;
import static baristation.bean.domain.QProduct.product;
import static baristation.bean.domain.QProductFlavorNote.productFlavorNote;
import static baristation.bean.domain.QRoaster.roaster;
import static baristation.bean.domain.QProductBookmark.productBookmark;

@Repository
@RequiredArgsConstructor
public class BeanProductRepositoryImpl implements BeanProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<BeanProduct> searchProductsWithFilters(ProductSearchRequest request, Pageable pageable) {
        BooleanExpression keywordCondition = keywordContains(request == null ? null : request.keyword());
        BooleanExpression flavorCategoryCondition = flavorCategoryEq(request == null ? null : request.flavorCategory());
        BooleanExpression acidityCondition = acidityBetween(
                request == null ? null : request.minAcidity(),
                request == null ? null : request.maxAcidity()
        );
        BooleanExpression sweetnessCondition = sweetnessBetween(
                request == null ? null : request.minSweetness(),
                request == null ? null : request.maxSweetness()
        );
        BooleanExpression bodyCondition = bodyBetween(
                request == null ? null : request.minBody(),
                request == null ? null : request.maxBody());
        BooleanExpression balanceCondition = balanceBetween(
                request == null ? null : request.minBalance(),
                request == null ? null : request.maxBalance());
        BooleanExpression roastingCondition = roastingEq(request == null ? null : request.roastingType());

        // content 쿼리: 실제 페이지 데이터 조회
        List<BeanProduct> content = queryFactory
                .selectFrom(beanProduct)
                .join(beanProduct.bean, bean).fetchJoin()
                .join(beanProduct.product, product).fetchJoin()
                .leftJoin(product.roaster, roaster).fetchJoin()
                .where(
                        keywordCondition,
                        flavorCategoryCondition,
                        acidityCondition,
                        sweetnessCondition,
                        bodyCondition,
                        balanceCondition,
                        roastingCondition
                )
                .orderBy(resolveOrderSpecifiers(request, pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // count 쿼리: fetch join 없이 전체 개수만 조회
        Long total = queryFactory
                .select(beanProduct.count())
                .from(beanProduct)
                .join(beanProduct.bean, bean)
                .join(beanProduct.product, product)
                .leftJoin(product.roaster, roaster)
                .where(
                        keywordCondition,
                        flavorCategoryCondition,
                        acidityCondition,
                        sweetnessCondition,
                        bodyCondition,
                        balanceCondition,
                        roastingCondition
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    @Override
    public Page<BeanProduct> searchProductsWithUserId(Pageable pageable, Long userId) {

        // userId가 없으면 빈 페이지 반환
        if (userId == null) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        // content 쿼리: 실제 페이지 데이터 조회
        // INNER JOIN으로 북마크된 상품만 직접 조회 (EXISTS보다 효율적)
        List<BeanProduct> content = queryFactory
                .selectFrom(beanProduct)
                .join(beanProduct.bean, bean).fetchJoin()
                .join(beanProduct.product, product).fetchJoin()
                .join(productBookmark).on(productBookmark.product.eq(product))
                .leftJoin(product.roaster, roaster).fetchJoin()
                .where(productBookmark.user.userId.eq(userId))
                .orderBy(resolveOrderSpecifiers(null, pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // count 쿼리: fetch join 없이 전체 개수만 조회
        Long total = queryFactory
                .select(beanProduct.count())
                .from(beanProduct)
                .join(beanProduct.bean, bean)
                .join(beanProduct.product, product)
                .join(productBookmark).on(productBookmark.product.eq(product))
                .leftJoin(product.roaster, roaster)
                .where(productBookmark.user.userId.eq(userId))
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }
    // --- 동적 쿼리를 위한 BooleanExpression 메서드들 ---

    private BooleanExpression keywordContains(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }

        String normalized = keyword.trim();
        return bean.nameKo.containsIgnoreCase(normalized)
                .or(bean.nameEn.containsIgnoreCase(normalized))
                .or(roaster.nameKo.containsIgnoreCase(normalized))
                .or(roaster.nameEn.containsIgnoreCase(normalized))
                .or(JPAExpressions
                        .selectOne()
                        .from(productFlavorNote)
                        .join(productFlavorNote.flavorNote, flavorNote)
                        .where(
                                productFlavorNote.product.eq(product),
                                flavorNote.nameKo.containsIgnoreCase(normalized)
                                        .or(flavorNote.nameEn.containsIgnoreCase(normalized))
                        )
                        .exists());
    }

    private BooleanExpression flavorCategoryEq(FlavorCategory category) {
        if (category == null) {
            return null;
        }

        return JPAExpressions
                .selectOne()
                .from(productFlavorNote)
                .join(productFlavorNote.flavorNote, flavorNote)
                .where(
                        productFlavorNote.product.eq(product),
                        flavorNote.flavorCategory.eq(category)
                )
                .exists();
    }

    private BooleanExpression acidityBetween(Double min, Double max) {
        return between(product.acidity, min, max);
    }

    private BooleanExpression sweetnessBetween(Double min, Double max) {
        return between(product.sweetness, min, max);
    }

    private BooleanExpression bodyBetween(Double min, Double max) {
        return between(product.body, min, max);
    }

    private BooleanExpression balanceBetween(Double min, Double max) {
        return between(product.balance, min, max);
    }
    private BooleanExpression roastingEq(RoastingType type) {
        return type != null ? product.roastLevel.eq(type) : null;
    }

    private BooleanExpression between(NumberPath<Double> path, Double min, Double max) {
        if (min == null && max == null) {
            return null;  // 범위가 없으면 조건 없음
        }

        BooleanExpression rangeExpression;
        if (min != null && max == null) {
            rangeExpression = path.goe(min);  // path >= min
        } else if (min == null) {
            rangeExpression = path.loe(max);  // path <= max
        } else {
            rangeExpression = path.between(min, max);  // min <= path <= max
        }

        // NULL 값도 허용하기 위해 OR로 연결
        return rangeExpression.or(path.isNull());
    }

    private NumberExpression<Integer> bitternessScoreExpression() {
        return new CaseBuilder()
                .when(product.roastLevel.eq(RoastingType.LIGHT)).then(1)
                .when(product.roastLevel.eq(RoastingType.MEDIUMLIGHT)).then(2)
                .when(product.roastLevel.eq(RoastingType.MEDIUM)).then(3)
                .when(product.roastLevel.eq(RoastingType.MEDIUMDARK)).then(4)
                .when(product.roastLevel.eq(RoastingType.DARK)).then(5)
                .otherwise(0);
    }

    private OrderSpecifier<?>[] resolveOrderSpecifiers(ProductSearchRequest request, Pageable pageable) {
        if (request != null && request.sortBy() != null) {
            // 한국어 기준 정렬 -> 다른 case의 경우 정렬 기준이 같다면 이름으로
            /**
             *     LATEST,         // 최신순
             *     NAME,           // 이름순
             *     ROASTING_LEVEL, // 로스팅 정도순
             *     ACIDITY,        // 산미순
             *     SWEETNESS,      // 단맛순
             *     BODY,           // 바디순
             *     BALANCE        // 밸런스순
             */
            return switch (request.sortBy()) {
                case NAME -> new OrderSpecifier<?>[]{bean.nameKo.asc(), bean.nameEn.asc(), product.productId.desc()};
                case ROASTING_LEVEL -> new OrderSpecifier<?>[]{bitternessScoreExpression().asc(), bean.nameKo.asc()};
                case ACIDITY -> new OrderSpecifier<?>[]{product.acidity.desc().nullsLast(), bean.nameKo.asc()};
                case SWEETNESS -> new OrderSpecifier<?>[]{product.sweetness.desc().nullsLast(), bean.nameKo.asc()};
                case BODY -> new OrderSpecifier<?>[]{product.body.desc().nullsLast(), bean.nameKo.asc()};
                case LATEST -> new OrderSpecifier<?>[]{product.createdAt.desc().nullsLast(), product.productId.desc().nullsLast()};
                case BALANCE -> new OrderSpecifier<?>[]{product.balance.desc().nullsLast(), bean.nameKo.asc()};
            };
        }

        if (pageable != null && pageable.getSort().isSorted()) {
            Order order = pageable.getSort().stream().findFirst().orElse(null);
            if (order != null) {
                return orderFromPageable(order);
            }
        }

        return new OrderSpecifier<?>[]{product.createdAt.desc().nullsLast(), product.productId.desc().nullsLast()};
    }

    private OrderSpecifier<?>[] orderFromPageable(Order order) {
        boolean asc = order.getDirection() == Sort.Direction.ASC;

        return switch (order.getProperty()) {
            case "name", "beanNameKo" -> asc
                    ? new OrderSpecifier<?>[]{bean.nameKo.asc(), bean.nameEn.asc()}
                    : new OrderSpecifier<?>[]{bean.nameKo.desc(), bean.nameEn.desc()};
            case "roastingLevel", "roastLevel" -> asc
                    ? new OrderSpecifier<?>[]{bitternessScoreExpression().asc(), bean.nameKo.asc()}
                    : new OrderSpecifier<?>[]{bitternessScoreExpression().desc(), bean.nameKo.desc()};
            case "acidity" -> asc
                    ? new OrderSpecifier<?>[]{product.acidity.asc().nullsLast(), bean.nameKo.asc()}
                    : new OrderSpecifier<?>[]{product.acidity.desc().nullsLast(), bean.nameKo.asc()};
            case "body" -> asc
                    ? new OrderSpecifier<?>[]{product.body.asc().nullsLast(), bean.nameKo.asc()}
                    : new OrderSpecifier<?>[]{product.body.desc().nullsLast(), bean.nameKo.asc()};
            case "balance" -> asc
                    ? new OrderSpecifier<?>[]{product.balance.asc().nullsLast(), bean.nameKo.asc()}
                    : new OrderSpecifier<?>[]{product.balance.desc().nullsLast(), bean.nameKo.asc()};
            case "sweetness" -> asc
                    ? new OrderSpecifier<?>[]{product.sweetness.asc().nullsLast(), bean.nameKo.asc()}
                    : new OrderSpecifier<?>[]{product.sweetness.desc().nullsLast(), bean.nameKo.asc()};
            default -> asc
                    ? new OrderSpecifier<?>[]{product.createdAt.asc().nullsLast(), product.productId.asc().nullsLast()}
                    : new OrderSpecifier<?>[]{product.createdAt.desc().nullsLast(), product.productId.desc().nullsLast()};
        };
    }

    @Override
    public BeanProduct findByProductId(Long productId) {
        return queryFactory
                .selectFrom(beanProduct)
                .join(beanProduct.bean, bean).fetchJoin()
                .join(beanProduct.product, product).fetchJoin()
                .leftJoin(product.roaster, roaster).fetchJoin()
                .where(product.productId.eq(productId))
                .fetchOne();
    }
}
