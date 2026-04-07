-- 1. 로스터리 데이터
INSERT IGNORE INTO roasters (
    roaster_id, name_ko, name_en, homepage_url, description, created_at
) VALUES
      (1, '모모스 커피', 'Momos Coffee', 'https://momos.co.kr', '부산 기반 스페셜티 로스터리', NOW()),
      (2, '프릳츠 커피', 'Fritz Coffee', 'https://fritz.co.kr', '서울 기반 스페셜티 로스터리', NOW()),
      (3, '센터커피', 'Center Coffee', 'https://centercoffee.kr', '밸런스 좋은 라인업이 강점인 스페셜티 로스터리', NOW());

-- 2. 향미 데이터
INSERT IGNORE INTO tasting_notes (
    tasting_note_id, name_ko, name_en, description
) VALUES
      (1, '재스민', 'Jasmine', '꽃 향 중심의 화사한 향미'),
      (2, '복숭아', 'Peach', '부드럽고 달콤한 핵과류 향미'),
      (3, '꿀', 'Honey', '은은하고 점도감 있는 단맛'),
      (4, '리치', 'Lychee', '열대 과일 계열의 향미'),
      (5, '요거트', 'Yogurt', '산뜻하고 유제품 같은 질감'),
      (6, '시트러스', 'Citrus', '오렌지, 레몬 계열의 산뜻한 향미'),
      (7, '베리', 'Berry', '붉은 과일 계열의 산미 중심 향미'),
      (8, '초콜릿', 'Chocolate', '달콤쌉싸름한 카카오 향미'),
      (9, '카라멜', 'Caramel', '녹진한 당류 계열의 단맛'),
      (10, '헤이즐넛', 'Hazelnut', '고소하고 견과류 중심의 향미'),
      (11, '홍차', 'Black Tea', '깔끔하고 티라이크한 향미'),
      (12, '사과', 'Apple', '밝고 선명한 과일 산미');

-- 3. 원두 데이터 6개
INSERT IGNORE INTO beans (
    bean_id, roaster_id, name_ko, name_en, roast_level, process, variety,
    washing_station, region, altitude_min, altitude_max, release_ym,
    sensory_narrative, acidity_pct, sweetness_pct, body_pct, roast_level_pct, created_at
) VALUES
      (
          1, 1, '에티오피아 예가체프 코체레', 'Ethiopia Yirgacheffe Kochere',
          'LIGHT', 'Washed', 'Ethiopian Heirloom',
          'Kochere Washing Station', 'Yirgacheffe',
          1900, 2200, '2026-04',
          '재스민과 복숭아가 먼저 올라오고, 후미에 꿀 같은 단맛이 길게 이어지는 밝고 화사한 컵.',
          85, 78, 40, 20, NOW()
      ),
      (
          2, 1, '에티오피아 구지 우라가', 'Ethiopia Guji Uraga',
          'LIGHT', 'Natural', 'Ethiopian Heirloom',
          'Uraga Station', 'Guji',
          1950, 2250, '2026-04',
          '리치와 복숭아 중심의 과일감이 풍부하고 요거트 같은 산뜻한 질감이 인상적인 내추럴 커피.',
          82, 80, 45, 18, NOW()
      ),
      (
          3, 2, '케냐 니에리 AA', 'Kenya Nyeri AA',
          'MEDIUM_LIGHT', 'Washed', 'SL28, SL34, Ruiru 11',
          'Nyeri Factory', 'Nyeri',
          1700, 2000, '2026-04',
          '시트러스와 베리의 선명한 산미가 도드라지며 홍차처럼 깔끔하게 마무리되는 구조감 있는 컵.',
          88, 70, 48, 28, NOW()
      ),
      (
          4, 2, '과테말라 우에우에테낭고', 'Guatemala Huehuetenango',
          'MEDIUM', 'Washed', 'Bourbon, Caturra',
          'El Injerto', 'Huehuetenango',
          1600, 2000, '2026-04',
          '초콜릿과 카라멜의 달콤함 위에 사과 같은 산뜻함이 얹혀 밸런스가 좋은 데일리 컵.',
          55, 82, 62, 45, NOW()
      ),
      (
          5, 3, '콜롬비아 우일라 디카페인', 'Colombia Huila Decaf',
          'MEDIUM', 'Sugarcane Decaf', 'Castillo, Caturra',
          'Huila Mill', 'Huila',
          1500, 1900, '2026-04',
          '디카페인임에도 초콜릿과 헤이즐넛의 고소함이 뚜렷하고 카라멜 같은 단맛이 안정적으로 이어지는 컵.',
          42, 80, 68, 50, NOW()
      ),
      (
          6, 3, '르완다 냐마셰케', 'Rwanda Nyamasheke',
          'MEDIUM_LIGHT', 'Washed', 'Red Bourbon',
          'Nyamasheke Washing Station', 'Nyamasheke',
          1700, 2100, '2026-04',
          '사과와 홍차의 깨끗한 인상 위에 시트러스가 더해져 맑고 투명한 느낌을 주는 컵.',
          78, 72, 44, 30, NOW()
      );

-- 4. 원두-향미 매핑
INSERT IGNORE INTO bean_tasting_notes (
    bean_tasting_note_id, bean_id, tasting_note_id
) VALUES
      (1, 1, 1),
      (2, 1, 2),
      (3, 1, 3),

      (4, 2, 4),
      (5, 2, 2),
      (6, 2, 5),

      (7, 3, 6),
      (8, 3, 7),
      (9, 3, 11),

      (10, 4, 8),
      (11, 4, 9),
      (12, 4, 12),

      (13, 5, 8),
      (14, 5, 10),
      (15, 5, 9),

      (16, 6, 12),
      (17, 6, 11),
      (18, 6, 6);