ALTER TABLE transaction
    ADD external_id VARCHAR(255);

ALTER TABLE transaction
    ADD CONSTRAINT uc_transaction_externalid UNIQUE (external_id);
