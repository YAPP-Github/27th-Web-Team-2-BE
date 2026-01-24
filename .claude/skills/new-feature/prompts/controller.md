# Controller 구현 프롬프트

## 참조
- Context: `.claude/context/{feature}-context.md`
- 규칙: `app/api/CLAUDE.md`
- 패턴: `app/api/src/main/kotlin/com/nomoney/api/meetvote/`

## 작업 순서 (TDD)

### 1. Request/Response DTO 정의
`app/api/src/main/kotlin/com/nomoney/api/{feature}/model/`

### 2. Controller 테스트 작성
`app/api/src/test/kotlin/com/nomoney/api/{feature}/{Feature}ControllerTest.kt`

```kotlin
@WebMvcTest({Feature}Controller::class)
class {Feature}ControllerTest(
    private val mockMvc: MockMvc,
) : DescribeSpec({
    val service = mockk<{Feature}Service>()

    describe("GET /api/v1/{feature}") {
        it("200 OK") {
            every { service.get{Entity}(any()) } returns entity
            mockMvc.get("/api/v1/{feature}") { param("id", "1") }
                .andExpect { status { isOk() } }
        }
    }

    describe("POST /api/v1/{feature}") {
        it("201 Created") {
            every { service.create{Entity}(any()) } returns entity
            mockMvc.post("/api/v1/{feature}") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"field": "value"}"""
            }.andExpect { status { isCreated() } }
        }
    }
})
```

### 3. Controller 구현
`app/api/src/main/kotlin/com/nomoney/api/{feature}/{Feature}Controller.kt`
→ `app/api/CLAUDE.md` 규칙 따름 (Swagger 필수)

### 4. 테스트 실행
```bash
./gradlew :app:api:test
```

### 5. Context 체크리스트 업데이트
