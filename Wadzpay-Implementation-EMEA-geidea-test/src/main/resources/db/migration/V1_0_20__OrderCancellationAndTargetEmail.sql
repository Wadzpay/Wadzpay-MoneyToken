ALTER TABLE wadzpay_order
    ADD target_email VARCHAR DEFAULT null,
    ADD is_cancelled BOOLEAN NOT NULL DEFAULT false,
    ADD is_failed BOOLEAN NOT NULL DEFAULT false;
