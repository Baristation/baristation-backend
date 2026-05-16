package baristation.lesson.repository;

import baristation.lesson.domain.Lesson;
import baristation.lesson.enums.DifficultyLevel;
import baristation.lesson.enums.LessonCategory;
import baristation.lesson.enums.Region;
import baristation.lesson.payload.request.LessonSearchRequest;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static baristation.lesson.domain.QLesson.lesson;
import static baristation.user.domain.QUser.user;

@Repository
@RequiredArgsConstructor
public class LessonRepositoryImpl implements LessonRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    // 요청 필터와 페이징 조건을 QueryDSL 검색 쿼리로 변환해 클래스 목록과 전체 개수를 조회
    @Override
    public Page<Lesson> searchLessonsWithFilters(LessonSearchRequest request, Pageable pageable) {
        BooleanExpression keywordCondition = keywordContains(request == null ? null : request.keyword());
        BooleanExpression categoryCondition = categoryEq(request == null ? null : request.category());
        BooleanExpression regionCondition = regionEq(request == null ? null : request.region());
        BooleanExpression difficultyCondition = difficultyEq(request == null ? null : request.difficulty());

        List<Lesson> content = queryFactory
                .selectFrom(lesson)
                .join(lesson.hostUser, user).fetchJoin()
                .where(
                        keywordCondition,
                        categoryCondition,
                        regionCondition,
                        difficultyCondition
                )
                .orderBy(defaultOrderSpecifiers())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(lesson.count())
                .from(lesson)
                .join(lesson.hostUser, user)
                .where(
                        keywordCondition,
                        categoryCondition,
                        regionCondition,
                        difficultyCondition
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    // 제목, 부제목, 호스트 닉네임, 지역 정보에서 키워드가 포함된 클래스를 찾는 조건을 만든다.
    private BooleanExpression keywordContains(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }

        String normalized = keyword.trim();
        return lesson.title.containsIgnoreCase(normalized)
                .or(lesson.subtitle.containsIgnoreCase(normalized))
                .or(user.nickname.containsIgnoreCase(normalized))
                .or(lesson.region.containsIgnoreCase(normalized))
                .or(lesson.city.containsIgnoreCase(normalized))
                .or(lesson.place.containsIgnoreCase(normalized))
                .or(lesson.address.containsIgnoreCase(normalized));
    }

    // 카테고리가 정확히 일치하는 클래스를 찾는 조건을 만든다.
    private BooleanExpression categoryEq(String category) {
        LessonCategory normalizedCategory = LessonCategory.from(category);
        if (normalizedCategory == null) {
            return null;
        }

        return lesson.lessonCategory.eq(normalizedCategory);
    }

    // 지역에 입력 지역명이 포함된 클래스를 찾는 조건을 만든다.
    private BooleanExpression regionEq(String region) {
        Region normalizedRegion = Region.from(region);
        if (normalizedRegion == null) {
            return null;
        }

        // 프론트는 "제주" 같은 표시명을 보내지만, DB lessons.region에는 "JEJU" 같은 enum name이 저장되어 있다.
        // 입력값은 label로 검증하고, 실제 DB 비교는 저장 형식에 맞춰 enum name으로 수행한다.
        return lesson.region.eq(normalizedRegion.name());
    }

    // 난이도가 정확히 일치하는 클래스를 찾는 조건을 만든다.
    private BooleanExpression difficultyEq(DifficultyLevel difficulty) {
        return difficulty == null ? null : lesson.difficultyLevel.eq(difficulty);
    }

    // 최신순을 기본 정렬로 사용
    private OrderSpecifier<?>[] defaultOrderSpecifiers() {
        return new OrderSpecifier<?>[]{lesson.createdAt.desc().nullsLast(), lesson.lessonId.desc().nullsLast()};
    }
}
