# E-commerce Project Backend

## 📖 프로젝트 소개

Spring Boot 기반 이커머스 백엔드 프로젝트 

## ✨ 주요 기능

*   **회원 관리:** 회원가입, 로그인, 정보 수정, 탈퇴 등 기본적인 회원 기능 및 OAuth2를 이용한 소셜 로그인 기능
*   **상품 관리:** 상품 등록, 조회, 수정, 삭제 기능, 카테고리 관리(등록, 조회, 수정, 삭제)
*   **주문 관리:** 상품 주문 생성, 주문 조회, 주문 취소 기능
*   **장바구니:** 사용자가 원하는 상품을 담고 관리할 수 있는 장바구니 기능
*   **Q&A:** 상품 및 주문에 대한 고객 문의를 처리하는 Q&A 기능
*   **알림:** 주문 상태 변경 등 주요 이벤트 발생 시 사용자에게 실시간 알림
*   **관리자:** 관리자 전용 페이지를 통해 매출 조회, 회원, 상품, 주문, 재고 관리 등 시스템의 전반적인 데이터 관리

## 🛠️ 기술 스택

*   **언어:** Java 17
*   **프레임워크:** Spring Boot 4.0.5
*   **데이터베이스:**
    *   JPA (Java Persistence API)
    *   Redis (캐싱, 세션 관리 등)
    *   MySQL
*   **인증 및 인가:**
    *   Spring Security
    *   OAuth2 (Social Login) : Google 소셜 로그인
    *   JWT (JSON Web Token) : API 인증
*   **API 문서화:** Swagger 
*   **모니터링:**
    *   Spring Boot Actuator
    *   Prometheus
    *   Grafana

## 🚀 시작하기

### 1. 프로젝트 클론

```bash
git clone https://github.com/your-username/ecommerce-backend.git
cd ecommerce-backend
```

### 2. 환경 설정

`application.yml` (또는 `application.properties`) 파일에 데이터베이스, Redis, JWT secret key 등 환경에 맞는 설정을 입력합니다.

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/your-db
    username: your-username
    password: your-password
  redis:
    host: localhost
    port: 6379
  jwt:
    secret: your-secret-key
```

### 3. 애플리케이션 실행

```bash
./gradlew bootRun
```

애플리케이션이 성공적으로 실행되면 `http://localhost:8080` 에서 확인할 수 있습니다.

## 📝 API 문서

애플리케이션 실행 후 다음 URL에서 API 문서를 확인할 수 있습니다.

*   **Swagger UI:** `http://localhost:8080/swagger-ui.html`

## 📦 아키텍처 (예상)

(프로젝트의 아키텍처 다이어그램이나 간단한 설명을 추가하면 좋습니다.)

## 🤝 기여

이 프로젝트는 ... (기여 방법에 대한 안내를 추가할 수 있습니다.)
