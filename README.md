# 보험 서비스 ERD 및 API

자동차보험의 대물배상을 소재로 보험 가입부터 사고 보상까지 구현하는 서비스입니다.  
현재 상품 조회·관리, 고객 조회, 계약 가입·조회, 보험료 납부를 구현했습니다.  
앞으로 사고 접수, 보험사 심사, 보험금 모의 지급 기능으로 확장할 예정입니다.

## ERD

```mermaid
erDiagram
    customer ||--o{ insurance_contract: "가입"
    insurance_product ||--o{ insurance_contract: "가입 상품"
    insurance_contract ||--o| premium_ledger: "전액 모의 납부"

    customer {
        BIGINT id PK "자동 증가"
        VARCHAR name "20자, 필수"
        DATE birth_date "필수"
        VARCHAR gender "MALE 또는 FEMALE"
        DATE license_issued_date "필수"
    }

    insurance_product {
        BIGINT id PK "자동 증가"
        VARCHAR name "100자, 필수"
        TEXT description "선택"
        BIGINT coverage_limit "보장 한도, 원"
        BIGINT premium "고정 보험료, 원"
        INT coverage_period_days "보장 일수"
    }

    insurance_contract {
        BIGINT id PK "자동 증가"
        BIGINT customer_id FK "필수"
        BIGINT product_id FK "필수"
        DATETIME created_at "DB 생성 시각"
        DATETIME coverage_starts_at "보장 시작, 포함"
        DATETIME coverage_ends_at "보장 종료, 미포함"
        VARCHAR vehicle_number "20자, 필수"
        BIGINT coverage_limit "가입 당시 보장 한도"
        BIGINT premium "가입 당시 보험료"
        VARCHAR status "PENDING_PAYMENT 또는 ACTIVE"
    }

    premium_ledger {
        BIGINT id PK "자동 증가"
        BIGINT contract_id FK, UK "계약당 납부 최대 1건"
        BIGINT amount "납부한 금액, 원"
        VARCHAR payment_method "현재 MOCK"
        DATETIME paid_at "DB 저장 시각"
    }
```

## API 목록

| 메서드 | 주소                                       | 기능                         | 정상 응답      |
|--------|--------------------------------------------|------------------------------|----------------|
| GET    | `/api/product`                             | 상품 목록                    | 200, 배열      |
| GET    | `/api/product/{id}`                        | 상품 상세                    | 200, 객체      |
| POST   | `/api/product`                             | 상품 등록                    | 201, 본문 없음 |
| PATCH  | `/api/product`                             | JSON의 `id`로 상품 이름 수정 | 200, 본문 없음 |
| DELETE | `/api/product/{id}`                        | 상품 삭제                    | 204            |
| GET    | `/api/customer`                            | 고객 목록                    | 200, 배열      |
| GET    | `/api/customer/{id}`                       | 고객 상세                    | 200, 객체      |
| POST   | `/api/contract`                            | 계약 생성                    | 201, 생성된 ID |
| GET    | `/api/customer/{customerId}/contracts`     | 고객의 계약 목록             | 200, 배열      |
| GET    | `/api/contracts/{id}`                      | 계약 상세                    | 200, 객체      |
| POST   | `/api/contracts/{contractId}/mock-payment` | 모의 보험료 납부             | 200, 납부 객체 |
