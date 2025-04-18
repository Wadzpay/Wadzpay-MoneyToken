ALTER TABLE notification_data
    ADD COLUMN if not exists is_read BOOLEAN DEFAULT false;
