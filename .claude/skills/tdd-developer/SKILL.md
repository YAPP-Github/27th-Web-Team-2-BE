---
name: tdd-developer
description: Develops features following TDD (Test-Driven Development) methodology with Red-Green-Refactor cycle
argument-hint: "[feature description or requirement]"
---

# tdd-developer Skill

Implements new features using **Test-Driven Development (TDD)** approach following the Red-Green-Refactor cycle.

**Feature Request**: $ARGUMENTS

---

## TDD Principles

### Red-Green-Refactor Cycle
1. **🔴 Red**: Write a failing test that defines desired behavior
2. **🟢 Green**: Write minimal code to make the test pass
3. **🔵 Refactor**: Improve code quality while keeping tests green

### Core Values
- **Test First**: Always write tests before implementation code
- **Small Steps**: Make one small change at a time
- **Continuous Validation**: Run tests frequently to ensure nothing breaks
- **Clean Code**: Refactor to improve design without changing behavior

---

## Execution Flow

### Step 1: Requirement Analysis

#### 1.1 Understand the Feature Request
Analyze $ARGUMENTS to identify:
- **Business requirement**: What problem are we solving?
- **Acceptance criteria**: When is the feature considered complete?
- **Domain context**: Which domain/module does this belong to?
- **External dependencies**: What external systems are involved?

#### 1.2 Determine Affected Modules
Based on the project structure (domain, port, adapter, core, app):
- Which modules need changes?
- Do we need new domain objects?
- Do we need new port interfaces?
- Which layer does the main logic belong to?

#### 1.3 Identify Test Scenarios
List all test cases needed:
- **Happy path**: Normal successful scenarios
- **Edge cases**: Boundary conditions, empty inputs
- **Error cases**: Invalid inputs, exceptions
- **Business rules**: Domain-specific validations

---

### Step 2: 🔴 RED - Write Failing Tests

#### 2.1 Start with the Simplest Test
Choose the most basic test case that demonstrates the core functionality.

**DO NOT** implement any production code yet!

#### 2.2 Write Test Following Project Conventions
- Use appropriate test framework (JUnit, Kotest)
- Follow naming conventions from existing tests
- Place test in correct module and package
- Use descriptive test names with backticks

```kotlin
// Example: Starting with domain logic test
class UserServiceTest {

    @Test
    fun `should create user with valid email`() {
        // Given
        val email = "test@example.com"
        val name = "Test User"

        // When
        val user = userService.createUser(email, name)

        // Then
        assertThat(user.email).isEqualTo(email)
        assertThat(user.name).isEqualTo(name)
    }
}
```

#### 2.3 Run the Test - Expect Failure
Execute the test to verify it fails:
- Compilation error (class/method doesn't exist) ✅ Expected
- Test failure (wrong behavior) ✅ Expected
- Test passes ❌ Problem! Test might not be testing anything

**IMPORTANT**: The test MUST fail initially. If it passes, the test is invalid.

#### 2.4 Verify Test Failure Message
Check that the failure message clearly indicates:
- What was expected
- What actually happened
- Why the test failed

---

### Step 3: 🟢 GREEN - Make Tests Pass

#### 3.1 Write Minimal Implementation
Write the **simplest possible code** to make the test pass:
- Don't overthink or over-engineer
- Don't add features not covered by tests
- Hard-coded values are acceptable at this stage
- Focus on making THIS test pass

#### 3.2 Create Required Components
Following the project architecture, create in order:
1. **Domain objects** (if needed): Value classes, entities, enums
2. **Port interfaces** (if needed): Repository interfaces with domain terminology
3. **Core service** (if needed): Business logic using ports
4. **Adapter implementations** (if needed): Concrete implementations of ports
5. **API layer** (if needed): Controllers, DTOs for HTTP communication

#### 3.3 Run Tests - Expect Success
Execute all tests to verify:
- The new test passes ✅
- All existing tests still pass ✅
- No compilation errors ✅

If tests fail, fix the implementation (not the test!).

---

### Step 4: 🔵 REFACTOR - Improve Code Quality

#### 4.1 Identify Code Smells
Look for opportunities to improve:
- **Duplication**: Repeated code that can be extracted
- **Long methods**: Break down complex logic
- **Unclear names**: Rename for better readability
- **Magic values**: Extract constants
- **Tight coupling**: Apply dependency inversion

#### 4.2 Apply Refactoring Patterns
Common refactorings:
- Extract method/function
- Extract constant/variable
- Rename for clarity
- Introduce parameter object
- Replace conditional with polymorphism

#### 4.3 Keep Tests Green
After each refactoring:
- Run all tests immediately
- Tests must remain green ✅
- If tests fail, undo the refactoring and try differently

#### 4.4 Refactor Tests Too
Clean up test code:
- Extract common setup to fixtures or builders
- Remove duplication in test data
- Improve test readability
- Keep tests maintainable

---

### Step 5: Repeat the Cycle

#### 5.1 Choose Next Test Case
From the test scenarios identified in Step 1:
- Pick the next simplest test case
- Prioritize by business value
- Build incrementally

#### 5.2 Return to RED Phase
Write the next failing test and repeat the cycle:
- 🔴 RED: Write failing test
- 🟢 GREEN: Make it pass
- 🔵 REFACTOR: Improve code

#### 5.3 Continue Until Feature Complete
Repeat until all acceptance criteria are met:
- All test scenarios implemented
- All edge cases covered
- All error cases handled
- Code is clean and maintainable

---

### Step 6: Final Validation

#### 6.1 Run Full Test Suite
Execute all tests in the project:
```bash
./gradlew test
```

Verify:
- All tests pass ✅
- Test coverage is comprehensive
- No flaky or ignored tests

#### 6.2 Review Code Quality
Check the implementation:
- Follows project conventions
- No code smells remaining
- Proper error handling
- Appropriate logging
- Documentation where needed

#### 6.3 Verify Architecture Compliance
Ensure the implementation follows project structure:
- Domain objects are pure and immutable
- Ports use domain terminology (no implementation details)
- Adapters properly implement ports
- Core services contain business logic
- API layer handles HTTP concerns only

---

## TDD Best Practices

### Write Tests That
- ✅ Are independent and isolated
- ✅ Test one thing at a time
- ✅ Have clear arrange-act-assert structure
- ✅ Use descriptive names that explain the scenario
- ✅ Fail for the right reason

### Avoid
- ❌ Writing implementation before tests
- ❌ Writing multiple tests before making them pass
- ❌ Skipping the refactor step
- ❌ Changing tests to make code pass (unless the test is wrong)
- ❌ Testing implementation details instead of behavior

### When to Mock
- **DO mock**: External dependencies (databases, APIs, ports)
- **DON'T mock**: Domain objects, value classes, simple logic
- Use mocks to isolate the unit under test

---

## Output Format

After each TDD cycle, provide status:

```markdown
## TDD Cycle Progress

### Current Phase: 🔴 RED / 🟢 GREEN / 🔵 REFACTOR

### Test Case: {test scenario description}

### Files Modified
- 📝 Test: `{test file path}`
- 💻 Implementation: `{implementation file path}`

### Test Result
- ✅ Test passes
- ✅ All existing tests pass
- ✅ No compilation errors

### Next Steps
1. {next test case to implement}
2. {remaining scenarios}
```

At feature completion, provide final summary:

```markdown
## TDD Feature Implementation Complete

### Feature
{feature description}

### Test Coverage
- **Total test cases**: X
- **Happy path scenarios**: Y
- **Edge cases**: Z
- **Error cases**: W

### Implementation Summary
| Module | Component | Description |
|--------|-----------|-------------|
| domain | {class} | {description} |
| port | {interface} | {description} |
| adapter | {class} | {description} |
| core | {service} | {description} |
| app | {controller} | {description} |

### Architecture Compliance
- ✅ Domain objects are pure and immutable
- ✅ Ports use domain terminology
- ✅ Business logic in core module
- ✅ Clean separation of concerns

### All Tests Passing
```
./gradlew test
BUILD SUCCESSFUL
```
```

---

## Anti-Patterns to Avoid

### 1. Writing Code Before Tests
❌ **Wrong**: Implement feature first, add tests later
✅ **Right**: Write failing test, then implement

### 2. Testing Implementation Details
❌ **Wrong**: Test private methods, internal state
✅ **Right**: Test public behavior, outcomes

### 3. Skipping Refactoring
❌ **Wrong**: Move to next feature with messy code
✅ **Right**: Clean up after each green phase

### 4. Writing Too Many Tests at Once
❌ **Wrong**: Write 10 tests, then implement all
✅ **Right**: One test → make it pass → refactor → repeat

### 5. Changing Tests to Make Code Pass
❌ **Wrong**: Test fails → change the test
✅ **Right**: Test fails → fix the implementation (unless test is genuinely wrong)

---

## Example TDD Session

### Cycle 1: Basic Creation
1. 🔴 Write test: `should create user with valid data`
2. 🟢 Create User domain class with constructor
3. 🔵 No refactoring needed yet

### Cycle 2: Validation
1. 🔴 Write test: `should throw exception when email is invalid`
2. 🟢 Add email validation in User constructor
3. 🔵 Extract email validation to separate validator

### Cycle 3: Persistence
1. 🔴 Write test: `should save user to repository`
2. 🟢 Create UserRepository port and UserService
3. 🔵 Extract repository calls to use case methods

### Cycle 4: Error Handling
1. 🔴 Write test: `should throw exception when user already exists`
2. 🟢 Add duplicate check in service
3. 🔵 Improve error messages and exception types

---

## Integration with Other Skills

This TDD skill naturally integrates with other skills:

- **domain-port**: Use when RED phase requires new domain objects or ports
- **rdbms-data-handler**: Use when GREEN phase needs database implementation
- **restful-api-designer**: Use when GREEN phase needs API endpoints
- **test-writer**: Use for additional test coverage after TDD cycles

---
