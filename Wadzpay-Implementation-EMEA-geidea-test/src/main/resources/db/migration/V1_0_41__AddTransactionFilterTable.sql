CREATE TABLE IF NOT EXISTS transaction_filter
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
    date_from VARCHAR(255),
    date_to VARCHAR(255),
    directoin_filter VARCHAR(255),
    type_filter VARCHAR(255),
    other_filter VARCHAR(255),
    status_filter VARCHAR(255),
    digital_currency VARCHAR(255),
    option_one VARCHAR(255),
    option_two VARCHAR(255)
    );
