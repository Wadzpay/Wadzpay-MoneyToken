alter table expo_push_notification_token
    alter column name drop not null;
ALTER TABLE expo_push_notification_token
    ADD IF NOT EXISTS fcm_id VARCHAR(255);
ALTER TABLE expo_push_notification_token
    ADD IF NOT EXISTS device VARCHAR(255);
