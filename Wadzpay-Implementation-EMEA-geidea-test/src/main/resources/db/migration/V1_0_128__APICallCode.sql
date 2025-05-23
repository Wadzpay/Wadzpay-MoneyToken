-- A new table for API call code.;
create table if not exists api_call_code
(
    id   BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    api_action  VARCHAR NOT NULL,
    api_description  VARCHAR NULL,
    api_code VARCHAR NULL,
    is_active BOOLEAN DEFAULT true
);


-- Insert data in to api_call_code;

INSERT INTO api_call_code (api_action,
                           api_description,
                           api_code,
                                      is_active)
VALUES ('LOAD', 'Load the tokens into Issuer wallet.', '100',true),
       ('SELL', 'Sell the tokens from sender wallet.', '200',true),
       ('BUY', 'Buy - load the tokens into customer wallet.', '300',true),
       ('TRANSFER', 'Transfer - transfer the tokens to receiver.', '400',true),
       ('WITHDRAWAL', 'withdraw- Surrender the tokens to Issuer.', '500',true)
