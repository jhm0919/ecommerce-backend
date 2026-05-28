# k6 부하테스트 가이드

이 디렉터리는 주요 API의 성능 기준을 측정하기 위한 k6 스크립트를 관리합니다.

이번 부하테스트의 목적은 성능 최적화 자체가 아니라, 최적화 전에 기준 지표를 확보하는 것입니다.  
API별 p95, p99, 실패율, RPS를 측정하고 이후 개선 전/후 비교 자료로 활용합니다.

## 사전 준비

- 로컬 또는 Docker Compose로 애플리케이션 서버를 실행합니다.
- k6를 설치합니다.
- 부하테스트용 데이터가 필요합니다.
- 실제 토큰, 비밀번호, 운영 URL, 개인정보는 커밋하지 않습니다.

서버가 실행되지 않은 상태에서 k6를 실행하면 다음과 같은 연결 실패가 발생합니다.

```text
connectex: No connection could be made because the target machine actively refused it
```

## 성능 테스트 데이터 생성

성능 테스트용 seed data는 기본적으로 비활성화되어 있습니다.  
`perf` 프로필과 `PERFORMANCE_SEED_ENABLED=true`를 함께 지정했을 때만 실행됩니다.

PowerShell 예시:

```powershell
$env:SPRING_PROFILES_ACTIVE="local,perf"
$env:PERFORMANCE_SEED_ENABLED="true"
.\gradlew.bat bootRun
```

또는 `.env`에 다음 값을 둘 수 있습니다.

```properties
SPRING_PROFILES_ACTIVE=local,perf
PERFORMANCE_SEED_ENABLED=true
```

주요 seed 설정값:

```properties
PERFORMANCE_SEED_PRODUCT_COUNT=10000
PERFORMANCE_SEED_SKUS_PER_PRODUCT=3
PERFORMANCE_SEED_CANCEL_ORDER_COUNT=1000
```

seed runner는 `perf-category-001` 카테고리가 이미 존재하면 중복 생성을 건너뜁니다.  
서버 로그에서 부하테스트에 사용할 `orderProductId`, `orderSkuId`, `cancelMemberId`를 확인할 수 있습니다.

## 실행 방법

아래 명령어의 `{...}` 값은 예시가 아니라 실행 환경마다 직접 채워야 하는 값입니다.  
seed data를 다시 생성하거나 다른 로컬 DB를 사용하면 MySQL `AUTO_INCREMENT` 값이 달라지므로, 고정 ID를 문서나 스크립트에 그대로 믿고 사용하면 안 됩니다.

상품 목록 조회:

```powershell
k6 run -e BASE_URL=http://localhost:8080 performance/k6/product-list.js
```

상품 상세 조회:

```powershell
$env:PRODUCT_IDS="{productId1},{productId2},{productId3}"
k6 run -e BASE_URL=http://localhost:8080 -e PRODUCT_IDS=$env:PRODUCT_IDS performance/k6/product-detail.js
```

비회원 주문 생성:

```powershell
$env:ORDER_PRODUCT_ID="{orderProductId}"
$env:ORDER_SKU_ID="{orderSkuId}"
k6 run -e BASE_URL=http://localhost:8080 -e PRODUCT_ID=$env:ORDER_PRODUCT_ID -e SKU_ID=$env:ORDER_SKU_ID performance/k6/order-create.js
```

회원 주문 취소:

```powershell
$env:TOKEN="{accessToken}"
$env:CANCEL_ORDER_IDS="{orderId1},{orderId2},{orderId3}"
k6 run -e BASE_URL=http://localhost:8080 -e TOKEN=$env:TOKEN -e ORDER_IDS=$env:CANCEL_ORDER_IDS performance/k6/order-cancel.js
```

## 환경별 ID 확인 방법

부하테스트 실행 전 현재 DB의 ID를 먼저 확인합니다.

주문 생성 테스트용 상품/SKU 확인:

```sql
SELECT
    p.id AS product_id,
    p.name,
    s.id AS sku_id,
    s.sku_code,
    s.stock
FROM products p
JOIN skus s ON s.product_id = p.id
WHERE p.name = 'Perf Order Product';
```

위 결과의 `product_id`를 `ORDER_PRODUCT_ID`, `sku_id`를 `ORDER_SKU_ID`로 사용합니다.

상품 상세 테스트용 상품 ID 확인:

```sql
SELECT id, name, status
FROM products
WHERE name LIKE 'Perf %'
  AND status = 'ACTIVE'
ORDER BY id DESC
LIMIT 10;
```

위 결과의 `id`를 쉼표로 연결해 `PRODUCT_IDS`로 사용합니다.  
단종 상품이 섞이면 상품 상세 API 정책에 따라 404가 발생할 수 있으므로 `ACTIVE` 상품만 사용하는 것을 권장합니다.

주문 취소 테스트용 주문 ID 확인:

```sql
SELECT id, order_number, status
FROM orders
WHERE member_id = {tokenMemberId}
  AND status = 'PENDING'
ORDER BY id DESC
LIMIT 20;
```

주문 취소는 JWT의 `memberId`와 주문의 `member_id`가 일치해야 합니다.  
현재 프로젝트는 Google OAuth 기반으로 토큰을 발급하므로, 주문 취소 시나리오는 인증/seed 전략을 별도로 맞춘 뒤 실행합니다.

## 스크립트 설명

- `product-list.js`: 공개 상품 목록 조회 API를 반복 호출합니다.
- `product-detail.js`: 여러 상품 ID 중 하나를 선택해 상품 상세 조회 API를 호출합니다.
- `order-create.js`: 지정한 상품/SKU로 비회원 주문 생성 API를 호출합니다.
- `order-cancel.js`: 지정한 주문 ID를 대상으로 회원 주문 취소 API를 호출합니다.

## 결과 해석 기준

k6 결과에서 우선 확인할 지표는 다음과 같습니다.

- `http_req_duration`: 요청 응답 시간
- `p(95)`: 전체 요청 중 95%가 이 시간 안에 응답했다는 의미
- `p(99)`: 전체 요청 중 99%가 이 시간 안에 응답했다는 의미
- `http_req_failed`: 실패한 요청 비율
- `http_reqs`: 전체 요청 수
- RPS: 초당 처리 요청 수
- VU: 가상 사용자 수

예시:

```text
http_req_duration p(95)=342.99ms p(99)=538.63ms
http_req_failed   rate=0.00%
http_reqs         12.14/s
```

이 결과는 “실패 없이 초당 약 12건을 처리했고, 95%의 요청은 약 343ms 안에 응답했다”는 의미입니다.

## Before / After 기록 항목

성능 개선 전후 비교를 위해 아래 항목을 기록합니다.

- 테스트 일시
- 테스트 대상 API
- 실행 환경
- seed data 규모
- VU 수와 테스트 시간
- p95
- p99
- 실패율
- RPS
- 병목으로 의심되는 지점
- 변경 전/후 코드 또는 쿼리 요약
- Prometheus/Grafana 관찰 지표

## 주의사항

- 부하테스트는 반드시 로컬 또는 테스트 환경에서 실행합니다.
- 운영 DB나 운영 API를 대상으로 실행하지 않습니다.
- 주문 생성 테스트는 실제 DB에 주문 데이터를 생성합니다.
- 같은 DB를 계속 사용할 경우 테스트 데이터가 누적될 수 있습니다.
- JUnit 테스트는 `test` 프로필의 H2 DB를 사용하고, k6 부하테스트는 local/perf DB를 사용합니다.
