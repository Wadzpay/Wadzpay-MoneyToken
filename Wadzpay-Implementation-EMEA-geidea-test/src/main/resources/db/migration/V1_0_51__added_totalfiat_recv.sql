ALTER TABLE transaction
    ADD IF NOT EXISTS total_fiat_received numeric(40, 20) DEFAULT 0;
