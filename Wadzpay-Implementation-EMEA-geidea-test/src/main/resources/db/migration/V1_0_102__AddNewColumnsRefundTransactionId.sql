ALTER TABLE transaction
    ADD IF NOT EXISTS refund_transaction_id VARCHAR(255)
