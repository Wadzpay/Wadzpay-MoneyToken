CREATE TABLE IF NOT EXISTS notification_status
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
    requester_email VARCHAR(255),
    notification_status VARCHAR(255),
    uuid VARCHAR(255)
    );
