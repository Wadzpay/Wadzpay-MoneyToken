ALTER TABLE block_confirmation_log
    ADD COLUMN if not exists transfer_id VARCHAR;

ALTER TABLE block_confirmation_log
    ADD COLUMN if not exists type VARCHAR;
	
ALTER TABLE block_confirmation_log
    RENAME COLUMN transaction_id TO hash;