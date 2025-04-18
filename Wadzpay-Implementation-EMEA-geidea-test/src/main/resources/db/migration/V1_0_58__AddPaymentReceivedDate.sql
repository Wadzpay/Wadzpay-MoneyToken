ALTER TABLE transaction
    ADD IF NOT EXISTS payment_received_date TIMESTAMP;
