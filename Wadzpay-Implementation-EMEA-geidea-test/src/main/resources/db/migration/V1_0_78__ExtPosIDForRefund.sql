/*
for refund transaction. external id can different.
*/


ALTER TABLE transaction
    ADD IF NOT EXISTS ext_pos_id_refund VARCHAR(255),
    ADD IF NOT EXISTS ext_pos_transaction_id_refund VARCHAR(255),
    ADD IF NOT EXISTS ext_pos_logical_date_refund TIMESTAMP,
    ADD IF NOT EXISTS ext_pos_shift_refund VARCHAR(255),
    ADD IF NOT EXISTS ext_pos_actual_date_refund DATE,
    ADD IF NOT EXISTS ext_pos_sequence_no_refund VARCHAR(255),
    ADD IF NOT EXISTS ext_pos_actual_time_refund TIME;
