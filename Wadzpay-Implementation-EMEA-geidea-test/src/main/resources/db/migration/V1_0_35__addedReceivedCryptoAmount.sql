ALTER TABLE transaction_pos
    ADD IF NOT EXISTS digital_currency_received numeric(40,20);
