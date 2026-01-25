---
name: new-feature
description: 새로운 기능을 추가합니다. Sub Agent 병렬 실행으로 빠르게 구현합니다.
argument-hint: "[feature name] [description]"
---

# new-feature Skill

새로운 기능을 추가합니다.

**기능명**: $ARGUMENTS

## 실행 흐름

```
Phase 1: 요구사항 분석 & Context 생성
    ↓
Phase 2: [병렬] Domain+Port || Entity
    ↓
Phase 3: [병렬] Adapter || Service
    ↓
Phase 4: Controller + DTO
```
---


## Phase 공통 규칙

각 Phase가 완료되면 **빌드 확인 → 테스트 실행 → git commit** 순서로 작업합니다.
자세한 커밋 메시지는 각 Phase의 "완료 후 작업" 섹션을 참조하세요.

### Issue ID 추출
커밋 메시지에 issue-id를 붙이기 위해 브랜치명에서 추출합니다:
```bash
ISSUE_ID=$(git branch --show-current | sed -E 's/^[^/]+\///')
```
브랜치명이 `feat/31-some-feature` 형태라면 `31-some-feature`가 추출됩니다.

---

## Phase 1: 요구사항 분석 & Context 생성

### 1.1 사용자 요구사항 분석
$ARGUMENTS에서 기능명과 설명을 파싱합니다.

### 1.2 Context 파일 생성
`.claude/context/{feature}-context.md` 파일을 생성합니다.
템플릿: `.claude/skills/new-feature/context-template.md` 참조

Context 파일에 포함할 내용:
- 기능 개요
- 도메인 모델 설계 (필드, 타입)
- API 엔드포인트 목록
- 비즈니스 규칙
- 진행 상태 체크리스트

### Phase 1 완료 후 작업
Context 파일 생성이 완료되면 아래 작업을 실행합니다:
```bash
ISSUE_ID=$(git branch --show-current | sed -E 's/^[^/]+\///')
git add -A && git commit -m "$ISSUE_ID feat: {feature} Context 문서 생성"
```

---

## Phase 2: Domain + Port + Entity (병렬 실행)

**두 개의 Task를 동시에 실행합니다.**

### Task 1: Domain + Port 생성
Task tool을 사용하여 general-purpose agent 실행:

```
Context 파일 `.claude/context/{feature}-context.md`와
프롬프트 `.claude/skills/new-feature/prompts/domain-port.md`를 참조하여
Domain 모델과 Port 인터페이스를 구현해주세요.

1. 테스트 먼저 작성 (domain/src/test/kotlin/...)
2. Domain 모델 구현 (value class ID + data class)
3. Port 인터페이스 정의
4. 테스트 실행: ./gradlew :domain:test
5. Context 파일의 체크리스트 업데이트
```

### Task 2: Entity 생성 (병렬)
Task tool을 사용하여 general-purpose agent 실행:

```
Context 파일 `.claude/context/{feature}-context.md`와
프롬프트 `.claude/skills/new-feature/prompts/entity.md`를 참조하여
JPA Entity를 생성해주세요.

1. JPA Entity 클래스 (BaseJpaEntity 상속, of() 팩토리)
2. JpaRepository 인터페이스
3. DDL 스크립트
4. Context 파일의 체크리스트 업데이트
```

**Phase 2 완료 조건**: 두 Task 모두 완료

### Phase 2 완료 후 작업
두 Task가 완료되면 아래 작업을 순서대로 실행합니다:
```bash
./gradlew build
./gradlew test
ISSUE_ID=$(git branch --show-current | sed -E 's/^[^/]+\///')
git add -A && git commit -m "$ISSUE_ID feat: {feature} Domain, Port, Entity 구현"
```

---

## Phase 3: Adapter + Service (병렬 실행)

**두 개의 Task를 동시에 실행합니다.**

### Task 3: Adapter 구현
Task tool을 사용하여 general-purpose agent 실행:

```
Context 파일 `.claude/context/{feature}-context.md`와
프롬프트 `.claude/skills/new-feature/prompts/adapter.md`를 참조하여
Adapter를 구현해주세요.

1. Adapter 테스트 작성
2. Port 인터페이스 구현
3. toDomain()/toEntity() 변환 메서드
4. 테스트 실행: ./gradlew :adapter:rdb:test
5. Context 파일의 체크리스트 업데이트
```

### Task 4: Service 구현
Task tool을 사용하여 general-purpose agent 실행:

```
Context 파일 `.claude/context/{feature}-context.md`와
프롬프트 `.claude/skills/new-feature/prompts/service.md`를 참조하여
Service를 구현해주세요.

1. Service 테스트 작성 (MockK로 Repository 모킹)
2. Service 비즈니스 로직 구현
3. 예외 처리 (NotFoundException 등)
4. 테스트 실행: ./gradlew :core:test
5. Context 파일의 체크리스트 업데이트
```

**Phase 3 완료 조건**: 두 Task 모두 완료

### Phase 3 완료 후 작업
두 Task가 완료되면 아래 작업을 순서대로 실행합니다:
```bash
./gradlew build
./gradlew test
ISSUE_ID=$(git branch --show-current | sed -E 's/^[^/]+\///')
git add -A && git commit -m "$ISSUE_ID feat: {feature} Adapter, Service 구현"
```

---

## Phase 4: Controller + DTO

### Task 5: Controller 구현
Task tool을 사용하여 general-purpose agent 실행:

```
Context 파일 `.claude/context/{feature}-context.md`와
프롬프트 `.claude/skills/new-feature/prompts/controller.md`를 참조하여
Controller를 구현해주세요.

1. Request/Response DTO 정의
2. Controller 테스트 작성
3. Controller 구현 (Swagger 어노테이션 포함)
4. 테스트 실행: ./gradlew :app:api:test
5. Context 파일의 체크리스트 업데이트
```

### Phase 4 완료 후 작업
Task가 완료되면 아래 작업을 순서대로 실행합니다:
```bash
./gradlew build
./gradlew test
ISSUE_ID=$(git branch --show-current | sed -E 's/^[^/]+\///')
git add -A && git commit -m "$ISSUE_ID feat: {feature} Controller 구현"
```

---

## Phase 5: 마무리 작업

### Task 1: context 파일 제거
`.claude/context/{feature}-context.md` 파일을 제거합니다.

```bash
ISSUE_ID=$(git branch --show-current | sed -E 's/^[^/]+\///')
git add -A && git commit -m "$ISSUE_ID feat: context 파일 제거"
```
---

## 참조 파일

- 각 모듈 규칙: 해당 모듈의 `CLAUDE.md` 참조
- 기존 패턴 참조:
  - Domain: `domain/src/main/kotlin/com/nomoney/meeting/domain/`
  - Service: `core/src/main/kotlin/com/nomoney/meeting/service/MeetingService.kt`
  - Adapter: `adapter/rdb/src/main/kotlin/com/nomoney/meeting/adapter/MeetingAdapter.kt`
  - Entity: `adapter/rdb/src/main/kotlin/com/nomoney/meeting/entity/`
  - Controller: `app/api/src/main/kotlin/com/nomoney/api/meetvote/`
