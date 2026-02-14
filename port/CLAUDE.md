# port 모듈

core에서 외부 컴포넌트를 호출하는 인터페이스 모듈입니다.

## 규칙
- 인터페이스명은 `Repository` 접미어 사용
- domain 모듈의 도메인 객체만 사용 (JPA Entity 사용 금지)

## 예시
```kotlin
interface MeetingRepository {
    fun findByMeetingId(meetingId: MeetingId): Meeting?
    fun save(meeting: Meeting): Meeting
}
```
