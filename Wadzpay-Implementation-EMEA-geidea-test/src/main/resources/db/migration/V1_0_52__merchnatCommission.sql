ALTER TABLE merchant
    ADD IF NOT EXISTS settlement_commission_percent numeric(40, 20) default 0.5,
    ADD IF NOT EXISTS merchant_commission_percent numeric(40, 20) default 0.0,
    ADD IF NOT EXISTS wadzpay_commission_percent numeric(40, 20) default 0.0;
