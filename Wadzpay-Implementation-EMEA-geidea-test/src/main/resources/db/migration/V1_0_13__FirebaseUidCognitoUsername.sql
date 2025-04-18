DELETE FROM user_account;

ALTER TABLE user_account
    DROP CONSTRAINT uk_c8lse8cjanuqwleyhcn2l2bl4;

ALTER TABLE user_account
    DROP COLUMN firebase_uid;

ALTER TABLE user_account
    ADD cognito_username VARCHAR(255) NOT NULL ;

ALTER TABLE user_account
    ADD CONSTRAINT uc_useraccount_cognitousername UNIQUE (cognito_username);
