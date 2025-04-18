ALTER TABLE user_account
    ADD IF NOT EXISTS first_name VARCHAR(100),
    ADD IF NOT EXISTS last_name VARCHAR(100);
