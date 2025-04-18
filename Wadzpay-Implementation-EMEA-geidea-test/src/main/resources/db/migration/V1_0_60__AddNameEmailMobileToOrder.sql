ALTER TABLE wadzpay_order
    ADD IF NOT EXISTS requester_user_name VARCHAR(255),
    ADD IF NOT EXISTS requester_email_address VARCHAR(255),
    ADD IF NOT EXISTS requester_mobile_number VARCHAR(255);
