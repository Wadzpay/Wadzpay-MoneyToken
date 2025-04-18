CREATE TABLE IF NOT EXISTS notification_data
(
    id
    BIGINT
    GENERATED
    BY
    DEFAULT AS
    IDENTITY
    NOT
    NULL,
    user_account_id BIGINT NOT NULL,
    requester_name VARCHAR(255),
    requester_email VARCHAR(255),
    requester_phone VARCHAR(255),
    receiver_name VARCHAR(255),
    receiver_email VARCHAR(255),
    receiver_phone VARCHAR(255),
    digital_currency VARCHAR(255),
    amount VARCHAR(255),
    fee VARCHAR(255),
    wallet_address VARCHAR(255),
    time_notification VARCHAR(255),
    title VARCHAR(255),
    body VARCHAR(255)
);
