# Domain + Port 생성 프롬프트

## 참조
- Context: `.claude/context/{feature}-context.md`
- 규칙: `domain/CLAUDE.md`, `port/CLAUDE.md`

## 작업 순서 (TDD)

### 1. Domain 테스트 작성
`domain/src/test/kotlin/com/nomoney/{feature}/domain/{Entity}Test.kt`

```kotlin
class {Entity}Test : DescribeSpec({
    describe("{Entity}") {
        it("ID와 필드가 올바르게 설정된다") {
            val entity = {Entity}(id = {Entity}Id("test"), ...)
            entity.id.value shouldBe "test"
        }
    }
})
```

### 2. Domain 모델 구현
`domain/src/main/kotlin/com/nomoney/{feature}/domain/{Entity}.kt`
→ `domain/CLAUDE.md` 규칙 따름

### 3. Port 인터페이스 정의
`port/src/main/kotlin/com/nomoney/{feature}/port/{Entity}Repository.kt`
→ `port/CLAUDE.md` 규칙 따름

### 4. 테스트 실행
```bash
./gradlew :domain:test
```

### 5. Context 체크리스트 업데이트
