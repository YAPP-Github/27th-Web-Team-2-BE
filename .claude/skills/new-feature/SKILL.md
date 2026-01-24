---
name: new-feature
description: 새로운 기능을 추가합니다.
disable-model-invocation: true
argument-hint: "[feature name]"
---

새로운 기능을 추가합니다. 기능명: $ARGUMENTS

아래 순서대로 파일을 생성해주세요. 각 모듈의 CLAUDE.md에 정의된 규칙을 따릅니다.

1. **Domain 모델** - `domain/src/main/kotlin/com/nomoney/{feature}/domain/`
2. **Port 인터페이스** - `port/src/main/kotlin/com/nomoney/{feature}/port/`
3. **JPA Entity** - `adapter/rdb/src/main/kotlin/com/nomoney/{feature}/entity/`
4. **Adapter 구현체** - `adapter/rdb/src/main/kotlin/com/nomoney/{feature}/adapter/`
5. **Service** - `core/src/main/kotlin/com/nomoney/{feature}/service/`
6. **Controller** - `app/api/src/main/kotlin/com/nomoney/api/{feature}/`
7. **Request/Response** - `app/api/src/main/kotlin/com/nomoney/api/{feature}/model/`
