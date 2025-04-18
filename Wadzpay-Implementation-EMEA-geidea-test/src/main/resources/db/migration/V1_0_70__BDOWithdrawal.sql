create table if not exists bdo_withdrawal
(
    id bigserial not null,
    card_number varchar not null,
    wallet_address varchar(255),
    amount numeric(40, 20),
    asset varchar(255),
    status varchar(255),
    created_at timestamp,
    CONSTRAINT order_details_pk PRIMARY KEY (id, card_number)
    );
