# Entity 생성 프롬프트

## 참조
- Context: `.claude/context/{feature}-context.md`
- 규칙 및 템플릿: `adapter/rdb/CLAUDE.md`

## 작업 순서

### 1. JPA Entity 생성
`adapter/rdb/src/main/kotlin/com/nomoney/{feature}/entity/{Entity}JpaEntity.kt`
→ `adapter/rdb/CLAUDE.md`의 Entity 템플릿 따름

### 2. JpaRepository 인터페이스
`adapter/rdb/src/main/kotlin/com/nomoney/{feature}/repository/{Entity}JpaRepository.kt`

### 3. DDL 스크립트
`adapter/rdb/src/main/resources/db/migration/V{version}__{description}.sql`
→ `adapter/rdb/CLAUDE.md`의 DDL 템플릿 따름

### 4. Context 체크리스트 업데이트
