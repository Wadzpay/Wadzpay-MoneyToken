ALTER TABLE merchant
    ADD IF NOT EXISTS order_exp_time_in_min bigint default 1;

ALTER TABLE transaction_pos
ADD IF NOT EXISTS conversion_rate numeric(40, 20),
    ADD IF NOT EXISTS total_fiat_received numeric(40, 20);
