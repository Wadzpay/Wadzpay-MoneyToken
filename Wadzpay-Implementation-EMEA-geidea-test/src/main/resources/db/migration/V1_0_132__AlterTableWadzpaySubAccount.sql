-- Alter wadzpay_subaccount table and added one new column user_account_id;
ALTER TABLE wadzpay_subaccount
    ADD COLUMN if not exists  user_account_id BIGINT NULL,
    ADD CONSTRAINT FK_wadzpay_user_account_id FOREIGN KEY (user_account_id) REFERENCES user_account (id);


