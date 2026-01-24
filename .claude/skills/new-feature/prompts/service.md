# Service 구현 프롬프트

## 참조
- Context: `.claude/context/{feature}-context.md`
- 예외 처리: `core/CLAUDE.md`
- 패턴: `core/src/main/kotlin/com/nomoney/meeting/service/MeetingService.kt`

## 작업 순서 (TDD)

### 1. Service 테스트 작성
`core/src/test/kotlin/com/nomoney/{feature}/service/{Feature}ServiceTest.kt`

```kotlin
class {Feature}ServiceTest : DescribeSpec({
    val repository = mockk<{Entity}Repository>()
    val service = {Feature}Service(repository)

    describe("create{Entity}") {
        it("생성하고 반환한다") {
            every { repository.save(any()) } returns expected
            service.create{Entity}(...) shouldBe expected
        }
    }

    describe("get{Entity}") {
        it("없으면 NotFoundException") {
            every { repository.findById(any()) } returns null
            shouldThrow<NotFoundException> { service.get{Entity}(id) }
        }
    }
})
```

### 2. Service 구현
`core/src/main/kotlin/com/nomoney/{feature}/service/{Feature}Service.kt`
→ `core/CLAUDE.md`의 예외 처리 패턴 따름

### 3. 테스트 실행
```bash
./gradlew :core:test
```

### 4. Context 체크리스트 업데이트
