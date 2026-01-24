# 프로젝트 소개
Kotiln을 사용하는 Spring Boot 프로젝트 입니다.

# 모듈 구조
## core
전역적으로 사용하는 도메인 객체와 비지니스 로직을 처리하는 모듈
## app
외부에서 들어오는 요청을 처리하는 모듈. 현재는 HTTP 통신이 있습니다.
Http 통신과 관련된 객체를 도메인 객체로 변경하여 core를 이용합니다.
## port
core에서 외부 컴포넌트를 호출하는 인터페이스 모듈입니다.
port 는 Repository 접미어를 가집니다.
## adaptor
port의 구현체 입니다. 아래와 같은 요소들이 있습니다.
- rdb
- redis
- kafka
- web client
