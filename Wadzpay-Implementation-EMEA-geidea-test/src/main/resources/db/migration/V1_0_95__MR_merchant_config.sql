ALTER TABLE transaction_refund_details
    ADD IF NOT EXISTS is_refund_reinitiate BOOLEAN default false;

ALTER TABLE transaction_refund_details
    ADD IF NOT EXISTS source_wallet_address  VARCHAR(255);
