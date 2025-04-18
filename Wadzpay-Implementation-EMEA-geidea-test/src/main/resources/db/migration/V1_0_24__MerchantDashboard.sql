ALTER TABLE merchant
    ADD country_of_registration VARCHAR NOT NULL DEFAULT 'US',
    ADD registration_code VARCHAR NOT NULL DEFAULT '',
    ADD primary_contact_full_name VARCHAR NOT NULL DEFAULT '',
    ADD primary_contact_email VARCHAR NOT NULL DEFAULT '',
    ADD primary_contact_phone_number VARCHAR NOT NULL DEFAULT '',
    ADD company_type VARCHAR,
    ADD industry_type VARCHAR,
    ADD merchant_id VARCHAR;

ALTER TABLE user_account
    ADD merchant_id BIGINT,
    ADD CONSTRAINT fk_user_account_on_merchant FOREIGN KEY (merchant_id) REFERENCES merchant (id);
