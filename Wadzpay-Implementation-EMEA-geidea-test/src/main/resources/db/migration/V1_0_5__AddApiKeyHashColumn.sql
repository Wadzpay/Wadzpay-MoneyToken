DELETE FROM merchant_api_key;

ALTER TABLE merchant_api_key
    ADD api_key_secret_hash VARCHAR NOT NULL;
