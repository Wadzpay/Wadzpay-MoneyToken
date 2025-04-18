
-- Alter issuance_banks table and added one new column default_fiat_currency.
ALTER TABLE public.issuance_banks
    ADD COLUMN if not exists  fiat_currency VARCHAR;

-- Alter issuance_banks table and rename destination_currency as destination_fiat_currency.
 ALTER TABLE public.issuance_banks
     RENAME COLUMN destination_currency TO destination_fiat_currency;




