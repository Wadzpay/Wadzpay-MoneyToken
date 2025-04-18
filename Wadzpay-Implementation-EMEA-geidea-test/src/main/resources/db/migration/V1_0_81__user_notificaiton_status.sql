ALTER TABLE user_account
    ADD COLUMN if not exists notification_status BOOLEAN;
