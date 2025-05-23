create table if not exists issuance_transaction_type
(
    id   BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    transaction_type_id  VARCHAR NOT NULL,
    transaction_type  VARCHAR NOT NULL,
    created_at timestamp NULL,
    is_active BOOLEAN DEFAULT true,
    CONSTRAINT pk_issuance_transaction_type PRIMARY KEY (id)
    );

create table if not exists issuance_transaction_limit_config
(
    id   BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    issuance_banks_id BIGINT NOT NULL,
    currency  VARCHAR NOT NULL,
    transaction_type_id  VARCHAR NOT NULL,
    frequency  VARCHAR NULL,
    count  VARCHAR NOT NULL,
    min_value numeric(20, 2),
    max_value numeric(20, 2),
    user_category  VARCHAR NULL,
    created_date timestamp NULL,
    is_active BOOLEAN DEFAULT true,
    created_by BIGINT NOT NULL,
    modified_date timestamp NULL,
    modified_by BIGINT NULL,
    CONSTRAINT pk_issuance_transaction_limit_config PRIMARY KEY (id),
    CONSTRAINT fk_issuance_banks_id FOREIGN KEY (issuance_banks_id) REFERENCES issuance_banks (id),
    CONSTRAINT fk_created_by FOREIGN KEY (created_by) REFERENCES issuance_banks (id)
    );


-- update issuance_transaction_type;

INSERT INTO issuance_transaction_type (transaction_type_id,
                                       transaction_type,
                                      created_at,
                                      is_active)
VALUES ('TTC_001', 'Initial Loading', '2023-03-20 10:31:49.735000',true),
       ('TTC_002', 'Subsequent Loading', '2023-03-20 10:31:49.735000',true),
       ('TTC_003', 'Purchase', '2023-03-20 10:31:49.735000',true),
       ('TTC_004', 'Merchant Offline', '2023-03-20 10:31:49.735000',true),
       ('TTC_005', 'Customer Offline', '2023-03-20 10:31:49.735000',true),
       ('TTC_006', 'Unspent Digital Currency Refund', '2023-03-20 10:31:49.735000',true),
       ('TTC_007', 'P2P Transfer', '2023-03-20 10:31:49.735000',true);
