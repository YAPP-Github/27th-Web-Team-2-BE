# adapter:rdb 모듈

port 모듈의 Repository 인터페이스 구현체입니다. JPA, QueryDSL, PostgreSQL을 사용합니다.

## Entity 규칙
- 모든 Entity는 `BaseJpaEntity` 상속 필수
- 모든 필드에 `@Column` 어노테이션 명시
- 필드는 생성자가 아닌 프로퍼티로, 가급적 `lateinit var` 사용
- companion object에 팩토리 메서드 `of()` 정의

### Entity 템플릿
```kotlin
@Entity
@Table(name = "{table_name}")
class {EntityName}JpaEntity : BaseJpaEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "{id_column}")
    var id: Long? = null

    @Column(name = "{column_name}")
    lateinit var fieldName: String

    companion object {
        fun of(
            id: Long? = null,
            fieldName: String,
        ): {EntityName}JpaEntity {
            return {EntityName}JpaEntity().apply {
                this.id = id
                this.fieldName = fieldName
            }
        }
    }
}
```

## DDL 규칙
- CREATE 시 `IF NOT EXISTS` 추가
- 모든 테이블에 `created_at`, `updated_at` 필드 필수

### DDL 템플릿
```sql
CREATE TABLE IF NOT EXISTS {table_name} (
    {id_column}   BIGSERIAL PRIMARY KEY,
    {column_name} VARCHAR(255) NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_{table_name}_createdat ON {table_name}(created_at);
CREATE INDEX IF NOT EXISTS idx_{table_name}_updatedat ON {table_name}(updated_at);
```

## Adapter 규칙
- Port 인터페이스 구현
- `toDomain()`, `toEntity()` 변환 메서드 포함
