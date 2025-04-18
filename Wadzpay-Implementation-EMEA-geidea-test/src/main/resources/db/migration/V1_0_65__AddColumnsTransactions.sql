ALTER TABLE transaction
    ADD IF NOT EXISTS ext_pos_logical_date TIMESTAMP,
    ADD IF NOT EXISTS ext_pos_shift VARCHAR(255),
    ADD IF NOT EXISTS ext_pos_actual_date DATE,
    ADD IF NOT EXISTS ext_pos_actual_time TIME;
