# Adapter 구현 프롬프트

## 참조
- Context: `.claude/context/{feature}-context.md`
- 규칙: `adapter/rdb/CLAUDE.md`
- 패턴: `adapter/rdb/src/main/kotlin/com/nomoney/meeting/adapter/MeetingAdapter.kt`

## 작업 순서 (TDD)

### 1. Adapter 테스트 작성
`adapter/rdb/src/test/kotlin/com/nomoney/{feature}/adapter/{Entity}AdapterTest.kt`

```kotlin
class {Entity}AdapterTest : DescribeSpec({
    val jpaRepository = mockk<{Entity}JpaRepository>()
    val adapter = {Entity}Adapter(jpaRepository)

    describe("findById") {
        it("도메인 객체를 반환한다") {
            every { jpaRepository.findById(1L) } returns Optional.of(jpaEntity)
            adapter.findById({Entity}Id(1L))?.id?.value shouldBe 1L
        }
    }

    describe("save") {
        it("저장 후 도메인 객체를 반환한다") {
            every { jpaRepository.save(any()) } returns savedEntity
            adapter.save(entity).id.value shouldBe 1L
        }
    }
})
```

### 2. Adapter 구현
`adapter/rdb/src/main/kotlin/com/nomoney/{feature}/adapter/{Entity}Adapter.kt`
→ `adapter/rdb/CLAUDE.md`의 Adapter 규칙 따름

### 3. 테스트 실행
```bash
./gradlew :adapter:rdb:test
```

### 4. Context 체크리스트 업데이트
