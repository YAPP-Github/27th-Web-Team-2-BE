---
name: test-writer
description: Writes comprehensive test code for Kotlin Spring Boot project following existing patterns and conventions
argument-hint: "[target class/file path or feature description]"
---

# test-writer Skill

Generates **comprehensive test code** for the specified class, feature, or module following project conventions.

**Target**: $ARGUMENTS

---

## Step 1: Understand Testing Context

### 1.1 Identify Target Code
- Parse the target argument (file path, class name, or feature description)
- Locate the source file to be tested
- Read the source code to understand its functionality

### 1.2 Analyze Existing Test Patterns
Search for existing test files to understand project conventions:
- Test file naming conventions (e.g., `*Test.kt`, `*Tests.kt`)
- Test directory structure (`src/test/kotlin/...`)
- Common test frameworks and libraries used (JUnit 5, Mockito, MockK, Kotest, etc.)
- Testing annotations and patterns (`@SpringBootTest`, `@WebMvcTest`, `@DataJpaTest`, etc.)

### 1.3 Determine Test Type
Based on the target code, determine appropriate test types:
- **Unit Test**: For isolated business logic (core module)
- **Integration Test**: For component interactions
- **Repository Test**: For database operations (`@DataJpaTest`)
- **Controller Test**: For HTTP endpoints (`@WebMvcTest` or `@SpringBootTest`)
- **Service Test**: For service layer with mocking

### 1.4 Identify Dependencies
Analyze what needs to be mocked or injected:
- External dependencies (repositories, web clients, etc.)
- Domain objects required for test setup
- Test data builders or fixtures

---

## Step 2: Gather Testing Requirements

### 2.1 Understand Module Context
Read the module's `CLAUDE.md` to understand testing rules:
- Are there module-specific testing guidelines?
- What test coverage is expected?
- Any specific naming or organization rules?

### 2.2 Analyze Method Signatures
For each public method in the target class:
- Input parameters and their types
- Return types
- Thrown exceptions
- Business logic flow

### 2.3 Identify Test Scenarios
For each method, determine test cases:
- **Happy path**: Normal execution with valid inputs
- **Edge cases**: Boundary conditions, empty/null inputs
- **Error cases**: Invalid inputs, exception scenarios
- **Business rules**: Domain-specific validation and logic

---

## Step 3: Generate Test Code

### 3.1 Create Test File Structure
Follow the project's directory structure:
```
{module}/src/test/kotlin/{package}/...Test.kt
```

### 3.2 Write Test Class
Include appropriate annotations and setup. Refer to /examples for guidance.

### 3.3 Follow Kotlin Test Conventions
- Use backtick method names for readability: `` `should return user when valid id`() ``
- Use `given-when-then` or `arrange-act-assert` pattern
- Prefer `lateinit var` for injected dependencies
- Use data classes for test fixtures
- Leverage Kotlin's null safety and extension functions

### 3.4 Apply Appropriate Assertions
Use the project's assertion library:
- AssertJ: `assertThat(result).isEqualTo(expected)`
- Kotest matchers: `result shouldBe expected`
- JUnit assertions: `assertEquals(expected, result)`

### 3.5 Mock External Dependencies
```kotlin
// Using Mockito
given(repository.findById(1L)).willReturn(Optional.of(user))

// Using MockK
every { repository.findById(1L) } returns user
```

---

## Step 4: Ensure Test Quality

### 4.1 Verify Test Coverage
Ensure all public methods have test coverage:
- At least one happy path test per method
- Edge cases and error scenarios covered
- Business rules validated

### 4.2 Check Test Independence
- Each test should be independent and isolated
- Use `@BeforeEach` for common setup if needed
- Avoid test interdependencies

### 4.3 Add Test Documentation
- Use descriptive test names that explain the scenario
- Add comments for complex test setups
- Document any test data assumptions

### 4.4 Validate Test Execution
Run the tests to ensure they:
- Compile without errors
- Execute successfully
- Follow project conventions
- Provide meaningful feedback on failure

---

## Step 5: Output Test Code

### 5.1 Present Generated Tests
Show the complete test file with:
- Import statements
- Class annotations
- All test methods
- Helper methods or fixtures if needed

### 5.2 Provide Test Summary
```markdown
## Test Code Generated

### File Location
`{module}/src/test/kotlin/{package}/{ClassName}Test.kt`

### Test Coverage
- ✅ Method 1: happy path, edge case, error case
- ✅ Method 2: happy path, validation
- ✅ Method 3: ...

### Test Type
- Unit Test / Integration Test / Controller Test / etc.
```

### 5.3 Suggest Improvements (Optional)
If applicable, suggest:
- Additional test scenarios to consider
- Refactoring opportunities in the source code for better testability
- Test utilities or fixtures that could be shared

---

## Examples

Test writing examples are provided in separate files:

### JUnit Style
- **Service Test**: `example-junit-service.kt`
  - Uses `@SpringBootTest`
  - Dependency mocking with `@MockBean`
  - AssertJ assertions

- **Repository Test**: `example-junit-repository.kt`
  - Uses `@DataJpaTest`
  - Tests actual database operations

### Kotest FunSpec Style
- **Service Test**: `example-kotest-service.kt`
  - Extends `FunSpec`
  - Groups tests with `context` blocks
  - Uses MockK and Kotest matchers
  - Manages setup with `beforeTest`

- **Repository Test**: `example-kotest-repository.kt`
  - Combines `@DataJpaTest` with FunSpec
  - Kotest matchers (`shouldBe`, `shouldNotBeNull`)

- **Controller Test**: `example-kotest-controller.kt`
  - Combines `@WebMvcTest` with FunSpec
  - Tests HTTP requests with MockMvc
  - Validates JSON responses

---
