USE insurance_system;

CREATE TABLE insurance_product (
   id BIGINT NOT NULL AUTO_INCREMENT,
   name VARCHAR(100) NOT NULL,
   description TEXT,
   coverage_limit BIGINT NOT NULL,
   premium BIGINT NOT NULL,
   coverage_period_days INT NOT NULL,
   PRIMARY KEY (id)
);

INSERT INTO insurance_product (
    name,
    description,
    coverage_limit,
    premium,
    coverage_period_days
) VALUES (
 '학습용 자동차 대물배상',
 '대물배상 서비스 구현을 위한 상품',
 20000000,
 100000,
 365
)