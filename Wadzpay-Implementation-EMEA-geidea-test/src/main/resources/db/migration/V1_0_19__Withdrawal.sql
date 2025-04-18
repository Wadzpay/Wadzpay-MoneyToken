ALTER TABLE wadzpay_order
    ADD type VARCHAR(255) DEFAULT 'ORDER',
    ALTER target_id DROP NOT NULL;
