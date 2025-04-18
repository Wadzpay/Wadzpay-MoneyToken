ALTER TABLE ramp_order_entity
    DROP CONSTRAINT fk9ps6od8wxsm94e3n19h6g8h0m;

ALTER TABLE ramp_order_entity
    DROP CONSTRAINT uk_22lc2qj0tdqklx5x7vyd4rxgx;

DROP TABLE ramp_order_entity CASCADE;
