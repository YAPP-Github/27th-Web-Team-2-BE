## 모듈 구조

| 모듈명     | 설명                              |                        
|---------|---------------------------------|
| app     | Controller + Request / Response |                          
| core    | Service + Dto                   | 
| adapter | Repository / Domain             |                          
| support | 다른 플젝에서도 쓸 수 있을 정도의 Util        |  

## Git 컨밴션

### 커밋 컨밴션

**기본 구조**

```
type: subject

body (선택)
```

**type 종류**

```
feat: 기능 추가
fix: 버그 수정
refactor: 코드 리팩토링
docs: 주석 및 문서 수정
test: 테스트 코드 작성 또는 수정
chore: 빌드/패키지 설정 변경
rename: 파일/폴더명 변경/삭제
style: 코드 포맷팅
```

**커밋 예시**

```
feat: 로그인 기능 구현

Email 중복확인 API 개발

---

fix: 사용자 정보 누락 버그 해결

사용자 서비스 코드 수정
```

### 브렌치 전략

| 브렌치명    | 설명                            | 기타                 |
|---------|-------------------------------|--------------------|
| main    | 운영 서버와 동일한 형상                 |                    |
| sandbox | Sandbox 서버와 동일한 형상            |                    |
| base/*  | 큰 기능을 작업 할 때 완료된 PR을 모아두는 브렌치 |                    |
| feat/*  | 기능 개발                         | * 에 들어갈 내용은 작업 번호  |
| develop | 운영 배포 스탠바이                    | Github default 브렌치 |

**Base PR 예시**

```shell
git checkout -b base/example
git commit --allow-empty -m "For Base"
git checkout -b feat/example-1
```
