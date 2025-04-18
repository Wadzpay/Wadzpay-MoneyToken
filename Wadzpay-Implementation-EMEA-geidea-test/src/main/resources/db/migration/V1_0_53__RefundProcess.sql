create table if not exists refund_token
(
    id                    bigserial                          not null
        constraint pkey primary key,
    refund_transaction_id bigint references transaction (id) NOT NULL,
    created_at            timestamp,
    valid_for             timestamp
);

ALTER TABLE refund_token
    ADD IF NOT EXISTS transaction_refund_token uuid unique;

ALTER TABLE transaction
    ADD IF NOT EXISTS refund_user_name             varchar(255),
    ADD IF NOT EXISTS refund_user_mobile           varchar(255),
    ADD IF NOT EXISTS refund_user_email           varchar(255),
    ADD IF NOT EXISTS refund_fiat_type             varchar(255),
    ADD IF NOT EXISTS refund_wallet_address        varchar(255),
    ADD IF NOT EXISTS refund_date_time             timestamp,
    ADD IF NOT EXISTS refund_status                varchar(255)    default null,
    ADD IF NOT EXISTS refund_initiate_date         timestamp,
    ADD IF NOT EXISTS refund_settlement_date       timestamp,
    ADD IF NOT EXISTS refund_initiated_from        varchar(255),
    ADD IF NOT EXISTS refund_amount_digital        numeric(40, 20) default 0.0,
    ADD IF NOT EXISTS refund_amount_fiat           numeric(40, 20) default 0.0,
    ADD IF NOT EXISTS refund_token_id              bigint references refund_token (id),
    ADD IF NOT EXISTS refund_transaction_id        bigint references transaction (id),
    ADD IF NOT EXISTS refund_digital_currency_type varchar(255),
    ADD IF NOT EXISTS refund_reason                varchar(255),
    ADD IF NOT EXISTS refund_acceptance_comment    varchar(255),
    ADD IF NOT EXISTS refund_approval_comment      varchar(255),
    ADD IF NOT EXISTS refund_type      varchar(255);

ALTER TABLE merchant
    ADD IF NOT EXISTS default_fiat varchar default 'USD';
