# 실행 방법 및 동작 흐름

## 요구 사항
- Docker Engine (도커 실행 시)
- Java 17+ (로컬 실행 시)

## 실행 방법 (Docker 권장)
프로젝트 루트에서 실행:

```bash
docker compose up --build
```

접속:
- http://localhost:8080

## 실행 방법 (로컬)
프로젝트 루트에서 실행:

```bash
./gradlew bootRun
```

jar 빌드 후 실행:

```bash
./gradlew clean bootJar
java -jar build/libs/*.jar
```

## 설정
`application.yml`의 `execution.work-dir`는 컨테이너에서도 접근 가능한 호스트 경로여야 합니다.  
Docker 실행 시 `docker-compose.yml`의 `EXECUTION_WORK_DIR` 값과 동일하게 맞춰주세요.

## 로직 흐름 (WebSocket)
1) 클라이언트가 `/ws/compile`에 연결합니다.  
2) `start` 메시지로 코드 실행을 요청합니다.  
3) 실행 중 출력은 `output` 메시지로 실시간 스트리밍됩니다.  
4) 실행 종료 시 `result` 메시지로 `stdout`, `stderr`, `exitCode`가 전달됩니다.  
5) 실행 중 표준 입력은 `input` 메시지로 전달됩니다.
6) REST `/compile` 엔드포인트는 제공하지 않습니다.

## Swagger
- UI: `http://localhost:8080/swagger-ui/index.html`
- Spec: `http://localhost:8080/v3/api-docs`

## 응답 필드
- `result`: 성공/실패  
- `stdout`: 표준 출력  
- `stderr`: 표준 에러  
- `exitCode`: 종료 코드  
- `SystemOut`: 호환용 필드(기존 UI 출력용)

## 주의 사항
- 코드 실행은 **호스트 Docker 소켓**을 통해 별도 컨테이너에서 수행됩니다.  
- 컨테이너는 `--network none`으로 외부 네트워크 접근을 차단합니다.  
- 현재 지원 언어는 **Java, Python**입니다. (JavaScript 비활성화)
- 타임아웃은 제거되어 **프로세스가 종료될 때까지 대기**합니다.
