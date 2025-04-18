CREATE TABLE IF NOT EXISTS customer_offline_wrong_password_entry
(
    id
    BIGINT
    GENERATED
    BY
    DEFAULT AS
    IDENTITY
    NOT
    NULL,
    sender_id VARCHAR(255),
    sender_email VARCHAR(255),
    sender_name VARCHAR(255),
    receiver_id VARCHAR(255),
    receiver_email VARCHAR(255),
    receiver_name VARCHAR(255),
    created_at timestamp default(now())
);
