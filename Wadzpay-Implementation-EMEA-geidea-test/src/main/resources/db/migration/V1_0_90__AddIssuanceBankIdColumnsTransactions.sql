ALTER TABLE transaction
    ADD COLUMN if not exists  issuance_banks_id BIGINT NULL
   /* CONSTRAINT fk_issuance_banks_id FOREIGN KEY (issuance_banks_id) REFERENCES issuance_banks (id) */
;