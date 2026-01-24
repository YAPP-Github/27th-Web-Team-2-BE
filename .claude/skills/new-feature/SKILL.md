---
name: new-feature
description: TDD 기반으로 새로운 기능을 추가합니다. Sub Agent 병렬 실행으로 빠르게 구현합니다.
argument-hint: "[feature name] [description]"
---

# new-feature Skill

새로운 기능을 TDD 기반으로 추가합니다.

**기능명**: $ARGUMENTS

## 실행 흐름

```
Phase 1: 요구사항 분석 & Context 생성
    ↓
Phase 2: [병렬] Domain+Port(TDD) || Entity
    ↓
Phase 3: [병렬] Adapter(TDD) || Service(TDD)
    ↓
Phase 4: Controller + DTO (TDD)
    ↓
Phase 5: 테스트 & 정리
```

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

---

## Phase 2: Domain + Port + Entity (병렬 실행)

**두 개의 Task를 동시에 실행합니다.**

### Task 1: Domain + Port 생성 (TDD)
Task tool을 사용하여 general-purpose agent 실행:

```
Context 파일 `.claude/context/{feature}-context.md`와
프롬프트 `.claude/skills/new-feature/prompts/domain-port.md`를 참조하여
Domain 모델과 Port 인터페이스를 TDD로 구현해주세요.

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

---

## Phase 3: Adapter + Service (병렬 실행)

**두 개의 Task를 동시에 실행합니다.**

### Task 3: Adapter 구현 (TDD)
Task tool을 사용하여 general-purpose agent 실행:

```
Context 파일 `.claude/context/{feature}-context.md`와
프롬프트 `.claude/skills/new-feature/prompts/adapter.md`를 참조하여
Adapter를 TDD로 구현해주세요.

1. Adapter 테스트 작성
2. Port 인터페이스 구현
3. toDomain()/toEntity() 변환 메서드
4. 테스트 실행: ./gradlew :adapter:rdb:test
5. Context 파일의 체크리스트 업데이트
```

### Task 4: Service 구현 (TDD)
Task tool을 사용하여 general-purpose agent 실행:

```
Context 파일 `.claude/context/{feature}-context.md`와
프롬프트 `.claude/skills/new-feature/prompts/service.md`를 참조하여
Service를 TDD로 구현해주세요.

1. Service 테스트 작성 (MockK로 Repository 모킹)
2. Service 비즈니스 로직 구현
3. 예외 처리 (NotFoundException 등)
4. 테스트 실행: ./gradlew :core:test
5. Context 파일의 체크리스트 업데이트
```

**Phase 3 완료 조건**: 두 Task 모두 완료

---

## Phase 4: Controller + DTO (TDD)

### Task 5: Controller 구현 (TDD)
Task tool을 사용하여 general-purpose agent 실행:

```
Context 파일 `.claude/context/{feature}-context.md`와
프롬프트 `.claude/skills/new-feature/prompts/controller.md`를 참조하여
Controller를 TDD로 구현해주세요.

1. Request/Response DTO 정의
2. Controller 테스트 작성
3. Controller 구현 (Swagger 어노테이션 포함)
4. 테스트 실행: ./gradlew :app:api:test
5. Context 파일의 체크리스트 업데이트
```

---

## Phase 5: 테스트 & 정리

### 5.1 전체 테스트 실행
```bash
./gradlew test
```

### 5.2 빌드 확인
```bash
./gradlew build
```

### 5.3 Context 파일 삭제
테스트와 빌드가 성공하면 `.claude/context/{feature}-context.md` 파일을 삭제합니다.

### 5.4 결과 요약
생성된 파일 목록과 테스트 결과를 요약하여 보고합니다.

---

## 참조 파일

- 각 모듈 규칙: 해당 모듈의 `CLAUDE.md` 참조
- 기존 패턴 참조:
  - Domain: `domain/src/main/kotlin/com/nomoney/meeting/domain/`
  - Service: `core/src/main/kotlin/com/nomoney/meeting/service/MeetingService.kt`
  - Adapter: `adapter/rdb/src/main/kotlin/com/nomoney/meeting/adapter/MeetingAdapter.kt`
  - Entity: `adapter/rdb/src/main/kotlin/com/nomoney/meeting/entity/`
  - Controller: `app/api/src/main/kotlin/com/nomoney/api/meetvote/`
