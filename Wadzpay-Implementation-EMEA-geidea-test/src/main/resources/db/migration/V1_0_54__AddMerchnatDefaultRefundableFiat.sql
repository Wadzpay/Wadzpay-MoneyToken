ALTER TABLE merchant
    ADD IF NOT EXISTS default_refundable_fiat_value numeric(40, 20) default 500.00,
    ADD IF NOT EXISTS default_time_zone varchar default '+5:30';
