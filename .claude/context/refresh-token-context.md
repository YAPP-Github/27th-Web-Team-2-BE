# RefreshToken Context

## 생성일시
2026-01-25

## 기능 개요
토큰 발급 시 Access Token과 함께 Refresh Token을 반환하고, Refresh Token을 사용하여 새로운 Access Token을 발급받는 기능입니다.
RTR(Refresh Token Rotation) 정책을 도입하여 Refresh Token은 한 번만 사용 가능합니다.

---

## 도메인 모델

### RefreshTokenId (value class)
```kotlin
@JvmInline
value class RefreshTokenId(val value: Long)
```

### RefreshToken (data class)
| 필드 | 타입 | 설명 | 필수 |
|------|------|------|------|
| id | RefreshTokenId | 고유 식별자 | O |
| tokenValue | String | 리프레시 토큰 값 | O |
| userId | UserId | 사용자 ID | O |
| expiresAt | LocalDateTime | 만료 시간 | O |
| used | Boolean | 사용 여부 (RTR) | O |
| createdAt | LocalDateTime | 생성 시간 | O |

### TokenPair (data class) - 토큰 발급 시 반환용
| 필드 | 타입 | 설명 | 필수 |
|------|------|------|------|
| accessToken | AuthToken | 액세스 토큰 | O |
| refreshToken | RefreshToken | 리프레시 토큰 | O |

---

## API 엔드포인트

| Method | Path | 설명 | Request | Response |
|--------|------|------|---------|----------|
| POST | /api/v1/auth/token | 토큰 발급 (수정) | IssueTokenRequest | IssueTokenResponse (accessToken + refreshToken) |
| POST | /api/v1/auth/refresh | 토큰 갱신 | RefreshTokenRequest | RefreshTokenResponse |

---

## 비즈니스 규칙

1. **토큰 발급 시 Access Token + Refresh Token 동시 발급**
   - Access Token: 기존과 동일 (30일 만료)
   - Refresh Token: 새로 생성 (90일 만료)

2. **Refresh Token으로 Access Token 재발급**
   - Refresh Token 유효성 검증 (만료, 사용 여부)
   - 새로운 Access Token 발급
   - RTR 정책: 새로운 Refresh Token도 함께 발급

3. **RTR (Refresh Token Rotation) 정책**
   - Refresh Token은 한 번만 사용 가능
   - 사용된 Refresh Token은 `used = true`로 마킹
   - 이미 사용된 Refresh Token으로 요청 시 거부

4. **예외 처리**
   - 유효하지 않은 Refresh Token: UnauthorizedException
   - 만료된 Refresh Token: UnauthorizedException
   - 이미 사용된 Refresh Token: UnauthorizedException

---

## 진행 상태

### Phase 1: 요구사항 분석
- [x] Context 파일 생성

### Phase 2: Domain + Port + Entity
- [x] Domain 테스트 작성
- [x] RefreshToken 도메인 모델 구현
- [x] TokenPair 도메인 모델 구현
- [x] RefreshTokenRepository Port 인터페이스 정의
- [x] RefreshTokenJpaEntity 생성
- [x] RefreshTokenJpaRepository 인터페이스
- [x] DDL 스크립트 작성

### Phase 3: Adapter + Service
- [x] RefreshTokenAdapter 테스트 작성
- [x] RefreshTokenAdapter 구현
- [x] AuthService 테스트 수정/추가
- [x] AuthService 수정 (issueToken → issueTokenPair, refreshToken 메서드 추가)

### Phase 4: Controller + DTO
- [x] IssueTokenResponse DTO 수정 (refreshToken 추가)
- [x] RefreshTokenRequest/Response DTO 정의
- [x] AuthController 테스트 수정/추가
- [x] AuthController 수정 (refresh 엔드포인트 추가)

### Phase 5: 완료
- [ ] 전체 테스트 통과
- [ ] 빌드 성공

---

## 생성/수정될 파일 목록

### Domain
- [x] `domain/src/main/kotlin/com/nomoney/auth/domain/RefreshToken.kt` (신규)
- [x] `domain/src/main/kotlin/com/nomoney/auth/domain/TokenPair.kt` (신규)
- [x] `domain/src/test/kotlin/com/nomoney/auth/domain/RefreshTokenTest.kt` (신규)

### Port
- [x] `port/src/main/kotlin/com/nomoney/auth/port/RefreshTokenRepository.kt` (신규)

### Entity
- [x] `adapter/rdb/src/main/kotlin/com/nomoney/auth/entity/RefreshTokenJpaEntity.kt` (신규)
- [x] `adapter/rdb/src/main/kotlin/com/nomoney/auth/repository/RefreshTokenJpaRepository.kt` (신규)
- [x] `adapter/rdb/src/main/resources/sql.ddl/V4_CREATE_REFRESH_TOKENS.sql` DDL 스크립트 (신규)

### Adapter
- [x] `adapter/rdb/src/main/kotlin/com/nomoney/auth/adapter/RefreshTokenAdapter.kt` (신규)
- [x] `adapter/rdb/src/test/kotlin/com/nomoney/auth/adapter/RefreshTokenAdapterTest.kt` (신규)

### Service
- [x] `core/src/main/kotlin/com/nomoney/auth/service/AuthService.kt` (수정)
- [x] `core/src/test/kotlin/com/nomoney/auth/service/AuthServiceTest.kt` (수정/신규)

### Controller
- [x] `app/api/src/main/kotlin/com/nomoney/api/auth/AuthController.kt` (수정)
- [x] `app/api/src/main/kotlin/com/nomoney/api/auth/model/AuthHttp.kt` (수정)
- [x] `app/api/src/test/kotlin/com/nomoney/api/auth/AuthControllerTest.kt` (수정/신규)

---

## 메모
- 기존 AuthToken은 Access Token 역할로 유지
- RTR 정책으로 보안 강화 - Refresh Token 탈취 시에도 한 번 사용 후 무효화됨
- Refresh Token 만료 기간: 90일 (Access Token 30일보다 길게 설정)
