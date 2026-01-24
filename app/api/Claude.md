# app:api 모듈

RESTful API를 담당하는 모듈입니다. Request/Response 객체 변환 역할을 하며 비즈니스 로직은 core 모듈에 위임합니다.

## 규칙
- API path는 `/api/v1/` 형태로 시작 (v1은 버전)
- Controller Class에 `@RequestMapping` 사용 금지
- PathVariable 사용 금지 (RequestParam 사용)
- 모든 HTTP 통신 요소에 Swagger 설명 작성 필수

## 예외 처리
- `GlobalExceptionHandler`에서 모든 예외 일괄 처리
- core 모듈의 `NoMoneyException` 계층 구조 사용
