ALTER TABLE bdo_withdrawal
    ADD IF NOT EXISTS total_amount numeric(40, 20);
