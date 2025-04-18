ALTER TABLE transaction
    ADD IF NOT EXISTS sequence_number VARCHAR(255),
    ADD IF NOT EXISTS pos_logical_date TIMESTAMP,
    ADD IF NOT EXISTS pos_shift VARCHAR(255),
    ADD IF NOT EXISTS pos_date DATE,
    ADD IF NOT EXISTS pos_time TIME;
