ALTER TABLE fiat_sub_account
DROP COLUMN if exists user_reference;

ALTER TABLE fiat_sub_account
add if not exists user_reference varchar default 'ABC';

