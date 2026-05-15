package baristation.user.domain;

import baristation.common.domain.BaseTimeEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "career")
public class Career extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "career_id")
    private Long careerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User userId;

    @Column(name = "title")
    private String title;
}
