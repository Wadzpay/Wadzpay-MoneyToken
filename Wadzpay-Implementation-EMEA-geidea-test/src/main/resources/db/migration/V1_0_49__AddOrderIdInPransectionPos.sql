ALTER TABLE transaction_pos
    ADD IF NOT EXISTS order_id bigint
        constraint fk5t5otsosiwxigikhd0oeikahdsjhss
            references wadzpay_order;
