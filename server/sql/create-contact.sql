CREATE TABLE insurance_contract (
   id BIGINT NOT NULL AUTO_INCREMENT,
   customer_id BIGINT NOT NULL,
   product_id BIGINT NOT NULL,

   created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
   coverage_starts_at DATETIME NOT NULL,
   coverage_ends_at DATETIME NOT NULL,

   vehicle_number VARCHAR(20) NOT NULL,
   coverage_limit BIGINT NOT NULL,
   premium BIGINT NOT NULL,
   status VARCHAR(20) NOT NULL DEFAULT 'PENDING_PAYMENT',

    PRIMARY KEY (id),

    CONSTRAINT fk_contract_customer
        FOREIGN KEY (customer_id)
        REFERENCES customer(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_contract_product
        FOREIGN KEY (product_id)
        REFERENCES insurance_product(id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_contract_status
        CHECK ( status IN ('PENDING_PAYMENT', 'ACTIVE')),

   CONSTRAINT chk_contract_period
       CHECK (coverage_ends_at > coverage_starts_at),

   CONSTRAINT chk_contract_coverage_limit
       CHECK (coverage_limit > 0),

   CONSTRAINT chk_contract_premium
       CHECK (premium > 0)
);
