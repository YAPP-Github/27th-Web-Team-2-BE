기준 정보
- 기준 브랜치: `feat/79` (from `origin/feat/66`)
- 현재 작업 경로(worktree): `/Users/sangmin8817/.codex/worktrees/9352/27th-Web-Team-2-BE`
- 사용자 기준 프로젝트 경로: `/Users/sangmin8817/Desktop/DEV/YAPP 27기/27th-Web-Team-2-BE`

요청 목표
- 기존 로그인 기능(특히 Google)에 영향 없이
- Kakao 로그인 사용자의 OAuth 토큰을 활용해
- 백엔드에서 카카오 캘린더 이벤트 생성 API를 추가한다.

사전 더블체크 결과
1. 저장소 내 `AGENTS.md`는 없음. 가이드 파일은 `Claude.md` 및 모듈별 `CLAUDE.md/Claude.md`로 확인됨.
2. 현재 로그인 구조는 백엔드 OAuth 콜백 기반.
- `GET /api/v1/auth/oauth/google`
- `GET /api/v1/auth/oauth/kakao`
- 위치: `app/api/.../AuthController.kt`
3. 현재 `SocialAuthService`는 OAuth access token으로 사용자 정보 조회 후, 자체 JWT(access/refresh)만 발급하고 소셜 OAuth 토큰은 저장하지 않음.
- 위치: `core/.../SocialAuthService.kt`
4. 현재 OAuth 포트는 access token 문자열만 반환.
- `port/.../SocialOAuthClient.kt`
- `fun getAccessToken(...): String`
5. Kakao 토큰 DTO는 현재 `access_token`, `expires_in`, `token_type`만 보유.
- 위치: `adapter/oauth/.../KakaoTokenResponse.kt`
6. 전역 예외 응답 규약은 `app/api/.../GlobalExceptionHandler.kt` 기준.
7. 참고: 현재 브랜치에서 `/api/v1/host/meeting/finalize` 문자열은 검색되지 않음(별도 확인 필요).

무영향(회귀 최소화) 원칙
1. Google 로그인 경로/동작은 변경하지 않는다.
2. 공용 OAuth 인터페이스 대규모 변경은 피하고, Kakao 전용 흐름을 분리 추가한다.
3. 기존 인증 쿠키 포맷 및 OAuth 리다이렉트 동작은 유지한다.
4. 캘린더 등록 실패가 로그인/기존 모임 플로우를 깨지 않도록 트랜잭션/예외 경계를 분리한다.

권장 구현 범위
1. Kakao 토큰 저장 모델 추가
- 신규 domain/port/adapter:rdb 구성 + SQL DDL 추가
- 예시 필드: `user_id`, `provider(KAKAO)`, `access_token`, `refresh_token`, `access_token_expires_at`, `scope`, `created_at`, `updated_at`
2. Kakao OAuth 응답 확장
- `refresh_token`, `refresh_token_expires_in`, `scope` 등 추가
- Kakao client에 “토큰 상세 획득” 전용 메서드 추가
3. Kakao 로그인 성공 후 토큰 upsert 저장
- 사용자 매핑 완료 시 저장
- Google 경로는 기존 로직 유지
4. 카카오 캘린더 API 클라이언트 추가
- `kapi.kakao.com` 이벤트 생성 호출
- access token 만료 시 refresh token으로 재발급 후 1회 재시도
5. 백엔드 캘린더 등록 API 추가
- 인증 사용자 기준으로 모임 정보 받아 이벤트 생성
- 응답에 외부 이벤트 식별자 포함
- 필요 시 중복 등록 방지 저장 설계 포함

검증 기준(완료 정의)
1. Google 로그인 기존 테스트/동작 무변경
2. Kakao 로그인 성공 시 토큰 저장 확인
3. 신규 캘린더 API로 카카오 이벤트 생성 성공
4. access token 만료 상황에서 refresh + 재시도 성공
5. 실패 시 `GlobalExceptionHandler` 규약 준수
6. 기존 `/api/v1/auth/*` 및 미팅 관련 기존 API 회귀 없음

검증 커맨드
- `./gradlew test`
- 필요 시: `./gradlew :adapter:oauth:test :core:test :app:api:test :adapter:rdb:test`

추가 주의
- 프론트에서 `NEXT_PUBLIC_KAKAO_OAUTH_SCOPE`에 `talk_calendar` 포함이 필수.
- 기존 사용자 동의 상태에 따라 1회 재동의가 필요할 수 있음.
