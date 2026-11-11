USE insurance_system;

CREATE TABLE receipt (
   id BIGINT NOT NULL AUTO_INCREMENT,
   contract_id BIGINT NOT NULL,
   crash_time DATETIME NOT NULL,
   crash_place TEXT NOT NULL,
   crash_detail TEXT NOT NULL,
   bill_amount BIGINT,
   PRIMARY KEY (id),
   FOREIGN KEY (contract_id) REFERENCES insurance_contract(id) ON DELETE RESTRICT,
   CONSTRAINT chk_receipt_bill_amount CHECK (bill_amount IS NULL OR bill_amount >= 0)
);
