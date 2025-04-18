ALTER TABLE order_webhook
    ALTER COLUMN webhook_owner_id DROP NOT NULL;

ALTER TABLE order_webhook
    DROP CONSTRAINT uk_c624c0gphi9ajavyaqqfyq77x;
