
-- Asset Quantity: DROP asset quantity table It not required.
-- API Call Code: DROP api_call_code table It not required.
DROP TABLE IF EXISTS public.asset_Quantity CASCADE;
DROP TABLE IF EXISTS public.api_call_code CASCADE;


-- Alter issuance_transaction_type table and added one new column user_account_id.
ALTER TABLE public.issuance_transaction_type
    ADD COLUMN if not exists  issuance_banks_id BIGINT NULL,
    ADD CONSTRAINT fk_issuance_banks_id FOREIGN KEY (issuance_banks_id) REFERENCES issuance_banks (id);

-- Alter issuance_transaction_limit_config table and added one new column for gold program.

ALTER TABLE public.issuance_transaction_limit_config
    ADD COLUMN IF NOT EXISTS digital_currency VARCHAR,
    ADD COLUMN IF NOT EXISTS incremental_quantity VARCHAR,
    ADD COLUMN IF NOT EXISTS quantity_unit VARCHAR;

-- Alter issuance_transaction_type table and rename currency as fiat currency.
ALTER TABLE public.issuance_transaction_limit_config
    RENAME COLUMN currency TO fiat_currency;


