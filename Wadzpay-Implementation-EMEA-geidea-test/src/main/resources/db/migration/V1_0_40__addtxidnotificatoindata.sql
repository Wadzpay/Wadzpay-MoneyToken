ALTER TABLE notification_data
    ADD IF NOT EXISTS  transaction_id VARCHAR(255);
