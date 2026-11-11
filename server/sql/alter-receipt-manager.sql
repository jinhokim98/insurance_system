-- manager 테이블 생성 후, manager_id 컬럼이 없는 DB에서 한 번만 실행한다.
-- 이미 컬럼/외래키를 추가한 DB에는 해당 구문을 중복 실행하지 않는다.
USE insurance_system;

ALTER TABLE receipt
ADD COLUMN manager_id BIGINT NULL,
ADD CONSTRAINT fk_receipt_manager
    FOREIGN KEY (manager_id) REFERENCES manager(id) ON DELETE RESTRICT;
