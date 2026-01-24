# adapter:rdb 모듈

port 모듈의 Repository 인터페이스 구현체입니다. JPA, QueryDSL, PostgreSQL을 사용합니다.

## Entity 규칙
- 모든 Entity는 `BaseJpaEntity` 상속 필수
- 모든 필드에 `@Column` 어노테이션 명시
- 필드는 생성자가 아닌 프로퍼티로, 가급적 `lateinit var` 사용

## 테이블 필수 필드
모든 테이블 마지막에 추가:
```sql
created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자'
updated_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '마지막 수정 일자'
INDEX idx_createdat(`created_at`)
INDEX idx_updatedat(`updated_at`)
```

## DDL 작성 규칙
- CREATE 시 `IF NOT EXISTS` 추가
