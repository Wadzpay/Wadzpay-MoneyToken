ALTER TABLE user_bank_account
    ADD IF NOT EXISTS created_at     timestamp,
    ADD IF NOT EXISTS country_code VARCHAR(100)
