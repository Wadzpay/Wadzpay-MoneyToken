CREATE TABLE IF NOT EXISTS block_confirmation_log
(
    id              BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    transaction_id     VARCHAR   NOT NULL,
    wallet VARCHAR                                ,
    block_confirmation_count INT,
    created_at timestamp
);
