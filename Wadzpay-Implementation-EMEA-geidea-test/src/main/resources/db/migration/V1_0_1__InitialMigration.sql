create table if not exists account
(
    reference varchar(255) not null
        constraint account_pkey
            primary key
);

alter table account owner to postgres;

create table if not exists account_owner
(
    id bigserial not null
        constraint account_owner_pkey
            primary key
);

alter table account_owner owner to postgres;

create table if not exists asset
(
    identifier varchar(255) not null
        constraint asset_pkey
            primary key,
    unit varchar(255)
);

alter table asset owner to postgres;

create table if not exists commit
(
    reference varchar(255) not null
        constraint commit_pkey
            primary key
);

alter table commit owner to postgres;

create table if not exists merchant
(
    name varchar(255)
        constraint uk_hpejy4i1nk4u3251c6ba2pmpv
            unique,
    id bigint not null
        constraint merchant_pkey
            primary key
        constraint fkf97ylxtq3t8l27jm3ehvreys6
            references account_owner
);

alter table merchant owner to postgres;

create table if not exists merchant_api_key
(
    id bigserial not null
        constraint merchant_api_key_pkey
            primary key,
    valid boolean not null,
    merchant_id bigint
        constraint fkppo9gje1m11fbviemuwjw88yp
            references merchant
);

alter table merchant_api_key owner to postgres;

create table if not exists order_webhook
(
    id bigserial not null
        constraint order_webhook_pkey
            primary key,
    input_id varchar(255) not null
        constraint uk_30d9i54ce1ww8jrupx6k56n2w
            unique,
    target_url varchar(255) not null,
    trigger_url varchar(255) not null,
    uuid uuid
        constraint uk_r8wuhkk6bvdggtfuijn5687r
            unique,
    webhook_owner_id bigint not null
        constraint uk_c624c0gphi9ajavyaqqfyq77x
            unique
        constraint fkivr88wf8iro4cqpgwk38aeu7s
            references merchant
    );

alter table order_webhook owner to postgres;

create table if not exists response_history
(
    id bigserial not null
        constraint response_history_pkey
            primary key,
    idempotency_key varchar(255),
    idempotency_namespace varchar(255),
    request_body text,
    response_body varchar(255),
    response_code integer,
    constraint ukg66ppa22wumevwjhjvlyir3d2
        unique (idempotency_namespace, idempotency_key)
);

alter table response_history owner to postgres;

create table if not exists status_type
(
    reference varchar(255) not null
        constraint status_type_pkey
            primary key
);

alter table status_type owner to postgres;

create table if not exists status
(
    reference varchar(255) not null
        constraint status_pkey
            primary key,
    value varchar(255),
    account_reference varchar(255)
        constraint fk1cynxob87o69lv29m454og0xa
            references account,
    type_reference varchar(255)
        constraint fkowj3iogxbqon1ow44s87lt8mq
            references status_type
);

alter table status owner to postgres;

create table if not exists status_entry
(
    id bigserial not null
        constraint status_entry_pkey
            primary key,
    value varchar(255),
    commit_reference varchar(255)
        constraint fkgtsgruyq7b515i5ed6cp52u07
            references commit,
    status_reference varchar(255)
        constraint fkjjsosqrt5u2bw78bd1gw6k5ld
            references status
);

alter table status_entry owner to postgres;

create table if not exists status_type_values
(
    status_type_reference varchar(255) not null
        constraint fkfhyfaj80oyu2jyaohn0eamn9
            references status_type,
    vals varchar(255)
);

alter table status_type_values owner to postgres;

create table if not exists subaccount
(
    reference varchar(255) not null
        constraint subaccount_pkey
            primary key,
    balance varchar(255),
    account_reference varchar(255)
        constraint fk5fcgldc2vo7m46nolhh9n06kt
            references account,
    asset_identifier varchar(255)
        constraint fkkcs31yhlvdkryt1pcvvh9nk13
            references asset
);

alter table subaccount owner to postgres;

create table if not exists subaccount_entry
(
    id bigserial not null
        constraint subaccount_entry_pkey
            primary key,
    amount varchar(255),
    balance varchar(255),
    commit_reference varchar(255)
        constraint fk8d8epcerr6ylrm0xmr2faaa4g
            references commit,
    subaccount_reference varchar(255)
        constraint fk68auls510hq38753gow640rg2
            references subaccount
);

alter table subaccount_entry owner to postgres;

create table if not exists user_account
(
    email varchar(255)
        constraint uk_hl02wv5hym99ys465woijmfib
            unique,
    firebase_uid varchar(255)
        constraint uk_c8lse8cjanuqwleyhcn2l2bl4
            unique,
    phone_number varchar(255)
        constraint uk_9qqab9g1qlmxdkiawv6ttm5ko
            unique,
    id bigint not null
        constraint user_account_pkey
            primary key
        constraint fk5mnpyua7fcxs9l0t66uqg7rkh
            references account_owner
);

alter table user_account owner to postgres;

create table if not exists ramp_order_entity
(
    id bigserial not null
        constraint ramp_order_entity_pkey
            primary key,
    crypto_code varchar(255),
    fiat_code varchar(255),
    provider_id varchar(255)
        constraint uk_22lc2qj0tdqklx5x7vyd4rxgx
            unique,
    ramp_payment_method integer,
    ramp_tx_type integer,
    status integer,
    user_account_id bigint not null
        constraint fk9ps6od8wxsm94e3n19h6g8h0m
            references user_account
);

alter table ramp_order_entity owner to postgres;

create table if not exists wadzpay_account
(
    id bigserial not null
        constraint wadzpay_account_pkey
            primary key,
    reference varchar(255)
        constraint uk_nmrvhof8al7aqnpheagam19a1
            unique,
    owner_id bigint
        constraint fk7oppkh2rjl9i0h48ondcmj00e
            references account_owner
);

alter table wadzpay_account owner to postgres;

create table if not exists wadzpay_subaccount
(
    id bigserial not null
        constraint wadzpay_subaccount_pkey
            primary key,
    asset integer,
    reference varchar(255)
        constraint uk_2r23pomcnaj4p1d84a3hq7tuj
            unique,
    account_id bigint
        constraint fk24x3vtikraj48au3ee0l6pbi3
            references wadzpay_account
);

alter table wadzpay_subaccount owner to postgres;

create table if not exists transaction
(
    id bigserial not null
        constraint transaction_pkey
            primary key,
    amount varchar(255),
    asset integer,
    created_at timestamp,
    reference varchar(255),
    status integer,
    type integer,
    receiver_id bigint
        constraint fkjo2c4rhjk91di90wpvbrsrrpl
            references wadzpay_subaccount,
    sender_id bigint
        constraint fkfg16jmdkv11dftrts1gwxhjay
            references wadzpay_subaccount
);

alter table transaction owner to postgres;

create table if not exists wadzpay_order
(
    id bigserial not null
        constraint wadzpay_order_pkey
            primary key,
    amount varchar(255),
    created_at timestamp,
    currency integer,
    description varchar(255),
    external_order_id varchar(255),
    uuid uuid
        constraint uk_8ofbk7kr3rddbdfwkgwo6nqv9
            unique,
    source_id bigint
        constraint fkgi7f0c8tf9frg5yydwhbnen5m
            references account_owner,
    target_id bigint
        constraint fk4mel9b3dssaigxdeoia9jedts
            references account_owner,
    transaction_id bigint
        constraint fk5t5otsosiwxigikhd0oeicn2n
            references transaction
);

alter table wadzpay_order owner to postgres;

