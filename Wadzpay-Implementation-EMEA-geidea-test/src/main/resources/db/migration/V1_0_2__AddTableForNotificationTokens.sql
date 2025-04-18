CREATE TABLE IF NOT EXISTS expo_push_notification_token
(
    id              BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    name            VARCHAR                                 NOT NULL,
    user_account_id BIGINT                                  NOT NULL,
    CONSTRAINT pk_expopushnotificationtoken PRIMARY KEY (id)
);

ALTER TABLE expo_push_notification_token
    ADD CONSTRAINT FK_EXPOPUSHNOTIFICATIONTOKEN_ON_USER_ACCOUNT FOREIGN KEY (user_account_id) REFERENCES user_account (id);

ALTER TABLE expo_push_notification_token
    ADD CONSTRAINT uc_expopushnotificationtoken_name UNIQUE (name);

