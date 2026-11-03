CREATE TABLE customer (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(20) NOT NULL,
  birth_date DATE NOT NULL,
  gender VARCHAR(10) NOT NULL,
  license_issued_date DATE NOT NULL,

  PRIMARY KEY (id),
  CONSTRAINT chk_customer_gender
      CHECK (gender IN ('MALE', 'FEMALE'))
);
