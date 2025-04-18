ALTER TABLE transaction_wallet_fee_details
    ADD COLUMN IF NOT EXISTS fee_description varchar(500),
    ADD COLUMN IF NOT EXISTS fee_asset varchar(50)
