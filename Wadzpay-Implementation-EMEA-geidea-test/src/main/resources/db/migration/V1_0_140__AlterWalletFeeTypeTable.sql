
-- Alter issuance_wallet_fee_type table and added one new column user_account_id.
ALTER TABLE public.issuance_wallet_fee_type
    ADD COLUMN if not exists  issuance_banks_id BIGINT NULL,
    ADD CONSTRAINT fk_issuance_banks_id FOREIGN KEY (issuance_banks_id) REFERENCES issuance_banks (id);

-- Alter issuance_wallet_config table and added one new column for gold program.

ALTER TABLE public.issuance_wallet_config
    ADD COLUMN IF NOT EXISTS digital_currency VARCHAR,
    ADD COLUMN IF NOT EXISTS fee_type VARCHAR;

-- Alter issuance_wallet_config table and rename value as fee value.
ALTER TABLE public.issuance_wallet_config
    RENAME COLUMN value TO fee_value;

-- Alter issuance_wallet_config table and rename currency as fiat currency.
ALTER TABLE public.issuance_wallet_config
    RENAME COLUMN currency TO fiat_currency;


