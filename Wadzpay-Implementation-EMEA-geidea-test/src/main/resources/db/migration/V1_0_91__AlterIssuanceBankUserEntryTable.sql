ALTER TABLE issuance_banks_user_entry
    ADD COLUMN if not exists wallet_id VARCHAR NULL,
    ADD COLUMN if not exists created_at timestamp NULL,
    ADD COLUMN if not exists updated_at timestamp NULL;
