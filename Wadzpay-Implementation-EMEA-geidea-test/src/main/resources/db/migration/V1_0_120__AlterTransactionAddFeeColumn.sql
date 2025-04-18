ALTER TABLE transaction
    ADD COLUMN IF NOT EXISTS total_requested_amount numeric(40,20) DEFAULT 0.0,
    ADD COLUMN IF NOT EXISTS total_requested_amount_asset varchar(50),
    ADD COLUMN IF NOT EXISTS total_fee_Applied numeric(40,20) DEFAULT 0.0,
    ADD COLUMN IF NOT EXISTS total_fee_Applied_asset varchar(50)
