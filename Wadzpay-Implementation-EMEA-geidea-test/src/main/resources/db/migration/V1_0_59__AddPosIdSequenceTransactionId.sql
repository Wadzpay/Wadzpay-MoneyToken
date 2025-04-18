ALTER TABLE transaction
    ADD IF NOT EXISTS ext_pos_id VARCHAR(255),
    ADD IF NOT EXISTS ext_pos_sequence_no VARCHAR(255),
    ADD IF NOT EXISTS ext_pos_transaction_id VARCHAR(255);
