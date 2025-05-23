CREATE TABLE crypto_address
(
    id       BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    asset    VARCHAR(255),
    address  VARCHAR(255),
    CONSTRAINT pk_cryptoaddress PRIMARY KEY (id)
);

ALTER TABLE wadzpay_subaccount
    ADD address_id BIGINT;

ALTER TABLE wadzpay_subaccount
    ADD CONSTRAINT FK_WADZPAY_SUBACCOUNT_ON_ADDRESS FOREIGN KEY (address_id) REFERENCES crypto_address (id);
