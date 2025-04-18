ALTER TABLE wadzpay_order
    ADD column if not exists wallet_address VARCHAR DEFAULT null,
    ADD column if not exists is_third_party BOOLEAN NOT NULL DEFAULT false,
    ADD column if not exists order_status VARCHAR DEFAULT null;
