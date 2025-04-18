-- Alter user_account table and added two new column customer type and created date.;
ALTER TABLE user_account
    ADD COLUMN if not exists  customer_Type VARCHAR NULL,
    ADD COLUMN if not exists created_date timestamp NULL;


-- Alter issuance_banks_user_entry table and added a new column partner_institution_name.;

ALTER TABLE issuance_banks_user_entry
    ADD COLUMN if not exists partner_institution_name VARCHAR NULL;
