ALTER TABLE block_confirmation_log
    ADD COLUMN if not exists updated_at timestamp;