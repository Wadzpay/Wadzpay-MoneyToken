ALTER TABLE transaction
    ADD IF NOT EXISTS source_wallet_address VARCHAR(255);
