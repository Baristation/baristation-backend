# 블루/그린 배포

이 디렉터리에는 단일 VM 환경에서 백엔드 블루/그린 배포를 구성할 때 필요한 최소 파일들이 들어 있습니다.

## 파일 설명

- `compose.blue-green.yml`: `app-blue`, `app-green` 컨테이너를 서로 다른 포트로 실행합니다.
- `deploy.sh`: 현재 비활성 컨테이너를 실행하고 `/actuator/health` 체크 후 nginx 트래픽을 전환하고 이전 컨테이너를 정리합니다.
- `nginx-app.conf.example`: nginx upstream 설정 예시입니다.
- `app.env.example`: 운영 서버의 `app.env`를 작성할 때 참고할 수 있는 환경변수 예시입니다.

## 최초 서버 설정

1. 라즈베리파이에 Docker, Docker Compose, nginx, certbot을 설치합니다.
2. `deploy/compose.blue-green.yml`, `deploy/deploy.sh` 파일을 `/home/<user>/deploy` 로 복사합니다.
3. `/home/<user>/app.env` 파일을 생성하고 운영 환경변수를 채웁니다.
4. 필요하면 `deploy/app.env.example` 파일을 템플릿으로 사용합니다.
5. nginx 설정을 `/etc/nginx/sites-available/ssl.conf` 에 반영하고 활성화합니다.
6. 라즈베리파이 사용자 계정이 `docker` 명령과 `sudo nginx -s reload` 를 실행할 수 있어야 합니다.

## GitHub Actions 브랜치별 배포

- `develop`, `develop/be`: 라즈베리파이 테스트 서버로 배포
- `main`: GCP 운영 서버로 배포

## GitHub Actions 시크릿

테스트 서버용 시크릿:

- `TEST_DOCKER_USERNAME`: 테스트용 Docker Registry 계정명
- `TEST_DOCKER_PASSWORD`: 테스트용 Docker Registry 비밀번호 또는 토큰
- `TEST_DOCKER_IMAGE`: 테스트 서버에 배포할 이미지 이름
- `TEST_PI_HOST`: 라즈베리파이 공인 IP 또는 도메인
- `TEST_PI_USERNAME`: 라즈베리파이 접속 계정명
- `TEST_PI_SSH_KEY`: GitHub Actions에서 사용할 개인키
- `TEST_PI_PORT`: SSH 포트 번호

운영 서버용 시크릿:

- `GCP_GCE_SA_KEY`: GCP 서비스 계정 JSON 키
- `GCP_PROJECT_ID`: GCP 프로젝트 ID
- `GCP_ZONE`: GCP VM 존
- `GCP_INSTANCE_NAME`: 운영 VM 이름
- `GCP_INSTANCE_USER`: 운영 VM 접속 계정명
- `GCP_DOCKER_IMAGE`: 운영 서버에 배포할 이미지 이름

현재 워크플로우는 테스트 브랜치에서는 `linux/arm64` 이미지로 빌드한 뒤 라즈베리파이에 SSH 배포하고, `main` 브랜치에서는 GCP VM으로 배포합니다.

## 실행 포트

- `app-blue`: 애플리케이션 포트 `8080`, 액추에이터 포트 `8082`
- `app-green`: 애플리케이션 포트 `8081`, 액추에이터 포트 `8083`

외부 사용자 트래픽은 항상 nginx를 통해 들어오며, nginx upstream은 `127.0.0.1:8080` 또는 `127.0.0.1:8081` 중 하나를 바라보도록 전환됩니다.
