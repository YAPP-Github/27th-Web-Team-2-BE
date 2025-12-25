## 모듈 소개
Restful Api를 담당하는 모듈입니다.
Request Response 객체를 변환 하는 역할을 하며 비지니스 로직은 core 모듈에 위임합니다.

## 제약
- Http 통신 요소들은 Swagger 설명을 작성해야합니다.
- 사용자가 사용하는 api path는 "/api/v1/" 형태로 시작해야합니다. (v1은 버전)
- Controller Class에 @RequestMapping을 사용하지 않습니다.
