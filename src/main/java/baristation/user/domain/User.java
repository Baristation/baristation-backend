package baristation.user.domain;

import baristation.common.domain.BaseTimeEntity;
import baristation.user.enums.UserProvider;
import baristation.user.enums.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "unique_provider_id", columnNames = {"provider", "provider_id"}),
        @UniqueConstraint(name = "unique_nickname", columnNames = {"nickname"})
})
public class User extends BaseTimeEntity {
    /**
     * updatedAt, profileImageUrl, status -> 해당 데이터는 현재 불필요할 것 같아서 삭제했습니다!
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    // 카카오는 null일 수 있습니다.
    @Column(name = "email", length = 255)
    private String email;

    // OAuth: google, kakao, naver 등
    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 20)
    private UserProvider provider;

    // 해당 플랫폼에서 제공하는 고유 식별 번호 (sub, id 등)
    @Column(name = "provider_id", nullable = false, length = 255)
    private String providerId;

    @Column(name = "nickname", nullable = false, length = 50)
    private String nickname;

    /* 네이버/카카오에서 제공하는 성별, 연령대 등 추가 정보를 저장하고 싶을 때 사용 */
    // @Column(name = "gender", length = 10)
    // private String gender;

    // @Column(name = "age_range", length = 20)
    // private String ageRange;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private UserRole role = UserRole.USER;
    /**
     * 닉네임 업데이트 (검증은 Service에서 수행)
     * 이 메서드는 이미 검증된 닉네임만 받아서 업데이트합니다.
     */
    public void updateNickname(String newNickname) {
        this.nickname = newNickname;
    }
}