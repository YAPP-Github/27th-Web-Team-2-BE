# 테이블 요구사항
모든 테이블에는 아래 필드가 필수로 추가되어야 합니다. 가급적 마지막으로 순서로 추가해주세요.
```sql
created_at  TIMESTAMPTZ      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자' 
updated_at  TIMESTAMPTZ      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '마지막 수정 일자'
INDEX idx_createdat(`created_at`)
INDEX idx_updatedat(`updated_at`)
```

# Entity 규칙
- 모든 Entity는 BaseEntity를 상속 해야 합니다.
- 모든 필드에는 @Column 어노테이션을 명시적으로 사용해야합니다.
- 각 필드는 생성자가 아닌 프로퍼티로 존재해야하면 가급적 lateinit var을 사용해야합니다.


# DDL 작성 규칙
- CREATE 경우 IF NOT EXIST 를 추가해주세요.
