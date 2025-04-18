ALTER TABLE merchant_config
    ADD IF NOT EXISTS resend_threshold_max_seconds BIGINT default 0;
