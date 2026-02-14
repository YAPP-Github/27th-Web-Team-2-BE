# core 모듈

비즈니스 로직을 처리하는 모듈입니다. Service 클래스와 예외를 정의합니다.

## 예외 처리 패턴
- 커스텀 예외는 `NoMoneyException` sealed class 계층 구조 사용
- 클라이언트 에러: `ClientException` 상속
  - `NotFoundException`: 리소스를 찾을 수 없음 (E001)
  - `DuplicateContentException`: 중복 콘텐츠 (E002)

## 예외 생성 예시
```kotlin
throw NotFoundException("모임을 찾을 수 없습니다.", "ID: ${meetingId.value}")
```
