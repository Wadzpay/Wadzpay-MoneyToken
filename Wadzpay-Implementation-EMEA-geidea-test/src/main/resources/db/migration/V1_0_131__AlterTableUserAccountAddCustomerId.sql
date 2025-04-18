-- Alter user_account table and added one new column customer Id.;
ALTER TABLE user_account
    ADD COLUMN if not exists  customer_id VARCHAR NULL;

