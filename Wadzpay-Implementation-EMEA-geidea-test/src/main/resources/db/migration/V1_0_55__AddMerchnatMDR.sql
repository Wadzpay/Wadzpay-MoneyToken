ALTER TABLE merchant
    ADD IF NOT EXISTS mdr_percentage numeric(40, 20) default 0.00;
