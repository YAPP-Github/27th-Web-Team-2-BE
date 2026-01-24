---
name: test
description: 테스트를 실행합니다.
disable-model-invocation: true
argument-hint: "[test class or method name]"
---

테스트를 실행합니다.

전체 테스트:
```bash
./gradlew test
```

인자가 제공된 경우 해당 테스트만 실행:
```bash
./gradlew test --tests "$ARGUMENTS"
```

테스트 실패 시 실패 원인을 분석하고 수정 방안을 제시해주세요.
