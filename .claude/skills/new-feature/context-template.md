# {Feature Name} Context

## 생성일시
{timestamp}

## 기능 개요
{description}

---

## 도메인 모델

### {Entity}Id (value class)
```kotlin
@JvmInline
value class {Entity}Id(val value: {IdType})  // String 또는 Long
```

### {Entity} (data class)
| 필드 | 타입 | 설명 | 필수 |
|------|------|------|------|
| id | {Entity}Id | 고유 식별자 | O |
| ... | ... | ... | ... |

---

## API 엔드포인트

| Method | Path | 설명 | Request | Response |
|--------|------|------|---------|----------|
| GET | /api/v1/{feature} | 조회 | - | {Entity}Response |
| POST | /api/v1/{feature} | 생성 | Create{Entity}Request | Create{Entity}Response |
| PUT | /api/v1/{feature} | 수정 | Update{Entity}Request | {Entity}Response |
| DELETE | /api/v1/{feature} | 삭제 | - | - |

---

## 비즈니스 규칙

1. {규칙 1}
2. {규칙 2}
3. ...

---

## 진행 상태

### Phase 1: 요구사항 분석
- [x] Context 파일 생성

### Phase 2: Domain + Port + Entity
- [ ] Domain 테스트 작성
- [ ] Domain 모델 구현
- [ ] Port 인터페이스 정의
- [ ] JPA Entity 생성
- [ ] JpaRepository 인터페이스

### Phase 3: Adapter + Service
- [ ] Adapter 테스트 작성
- [ ] Adapter 구현
- [ ] Service 테스트 작성
- [ ] Service 구현

### Phase 4: Controller + DTO
- [ ] Request/Response DTO 정의
- [ ] Controller 테스트 작성
- [ ] Controller 구현

### Phase 5: 완료
- [ ] 전체 테스트 통과
- [ ] 빌드 성공
- [ ] Context 파일 삭제

---

## 생성된 파일 목록

### Domain
- [ ] `domain/src/main/kotlin/com/nomoney/{feature}/domain/{Entity}.kt`
- [ ] `domain/src/test/kotlin/com/nomoney/{feature}/domain/{Entity}Test.kt`

### Port
- [ ] `port/src/main/kotlin/com/nomoney/{feature}/port/{Entity}Repository.kt`

### Entity
- [ ] `adapter/rdb/src/main/kotlin/com/nomoney/{feature}/entity/{Entity}JpaEntity.kt`
- [ ] `adapter/rdb/src/main/kotlin/com/nomoney/{feature}/repository/{Entity}JpaRepository.kt`

### Adapter
- [ ] `adapter/rdb/src/main/kotlin/com/nomoney/{feature}/adapter/{Entity}Adapter.kt`
- [ ] `adapter/rdb/src/test/kotlin/com/nomoney/{feature}/adapter/{Entity}AdapterTest.kt`

### Service
- [ ] `core/src/main/kotlin/com/nomoney/{feature}/service/{Feature}Service.kt`
- [ ] `core/src/test/kotlin/com/nomoney/{feature}/service/{Feature}ServiceTest.kt`

### Controller
- [ ] `app/api/src/main/kotlin/com/nomoney/api/{feature}/{Feature}Controller.kt`
- [ ] `app/api/src/main/kotlin/com/nomoney/api/{feature}/model/` (Request/Response DTOs)
- [ ] `app/api/src/test/kotlin/com/nomoney/api/{feature}/{Feature}ControllerTest.kt`

---

## 메모
{작업 중 발견된 이슈나 참고사항}
