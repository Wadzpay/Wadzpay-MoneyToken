drop table entity_address CASCADE;
drop table entity_admin_details CASCADE;
drop table entity_contact_details CASCADE;
drop table entity_bank_details CASCADE;
drop table entity_info CASCADE;
drop table entity_others CASCADE;
drop table institution CASCADE;
drop table aggregator CASCADE;


create table if not exists entity_address
(

    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    entity_address_address_line1 VARCHAR(255),
    entity_address_address_line2 VARCHAR(255),
    entity_address_address_line3 VARCHAR(255),
    entity_address_city VARCHAR(255),
    entity_address_state VARCHAR(255),
    entity_address_country VARCHAR(255),
    entity_address_postal_code VARCHAR(255),
    constraint entity_address_pkey primary key (id)
);


create table if not exists entity_admin_details
(
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    entity_admin_details_first_name VARCHAR(255),
    entity_admin_details_middle_name VARCHAR(255),
    entity_admin_details_last_name VARCHAR(255),
    entity_admin_details_email_id VARCHAR(255),
    entity_admin_details_mobile_number VARCHAR(255),
    entity_admin_details_department VARCHAR(255),
    constraint entity_admin_details_pkey primary key (id)
);


create table if not exists entity_contact_details
(
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    entity_contact_details_first_name VARCHAR(255),
    entity_contact_details_middle_name VARCHAR(255),
    entity_contact_details_last_name VARCHAR(255),
    entity_contact_details_email_id VARCHAR(255),
    entity_contact_details_mobile_number VARCHAR(255),
    entity_contact_details_designation VARCHAR(255),
    entity_contact_details_department VARCHAR(255),
    constraint contact_pkey primary key (id)
);


create table if not exists entity_bank_details
(
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    entity_bank_details_bank_name VARCHAR(255),
    entity_bank_details_bank_account_number VARCHAR(255),
    entity_bank_details_bank_holder_name VARCHAR(255),
    entity_bank_details_branch_code VARCHAR(255),
    entity_bank_details_branch_location VARCHAR(255),
    constraint bank_id_pkey primary key (id)
);


create table if not exists entity_info
(
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    entity_info_abbrevation VARCHAR(255),
    entity_info_description VARCHAR(255),
    entity_info_logo VARCHAR(255),
    entity_info_region VARCHAR(255),
    entity_info_timezone VARCHAR(255),
    entity_info_type VARCHAR(255),
    entity_info_default_digital_currency VARCHAR(255),
    entity_info_base_fiat_currency VARCHAR(255),
    constraint info_id_pkey primary key (id)
);


create table if not exists entity_others
(
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    entity_others_customer_offline_txn VARCHAR(255),
    entity_others_merchant_offline_txn VARCHAR(255),
    entity_others_approval_work_flow VARCHAR(255),
    entity_others_activation_date VARCHAR(255),
    constraint others_id_pkey primary key (id)
);


create table if not exists aggregator
(
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    address_id BIGINT,
    admin_details_id BIGINT,
    bank_details_id BIGINT,
    contact_details_id BIGINT,
    info_id BIGINT,
    others_id BIGINT,
    aggregator_preference_id VARCHAR(255) constraint uk_aggregator_preference_id unique,
    aggregator_name VARCHAR(255),
    aggregator_status VARCHAR(255),
    constraint aggregator_pkey primary key (id)
);


create table if not exists institution
(
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    aggregator_preference_id  VARCHAR(255),
    insitution_preference_id VARCHAR(255),
    insitution_name VARCHAR(255),
    insitution_status VARCHAR(255),
    address_id BIGINT,
    admin_details_id BIGINT,
    bank_details_id BIGINT,
    contact_details_id BIGINT,
    info_id BIGINT,
    others_id BIGINT,
    constraint institution_pkey primary key (id),
    constraint fk_instition_aggregator_id foreign key(aggregator_preference_id) references aggregator(aggregator_preference_id)
)
