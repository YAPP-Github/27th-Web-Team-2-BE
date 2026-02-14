# domain 모듈

순수 도메인 객체를 정의하는 모듈입니다. 외부 의존성이 없습니다.

## 규칙
- `data class`로 도메인 모델 정의
- ID는 `@JvmInline value class`로 정의하여 타입 안전성 확보
- 프레임워크 의존성 금지 (Spring, JPA 등)

## 예시
```kotlin
@JvmInline
value class MeetingId(val value: String)

data class Meeting(
    val id: MeetingId,
    val title: String,
    // ...
)
```
