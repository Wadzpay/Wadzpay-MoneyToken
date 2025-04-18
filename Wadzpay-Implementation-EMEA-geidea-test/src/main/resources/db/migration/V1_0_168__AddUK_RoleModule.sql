ALTER TABLE role
    ADD Column if not exists aggregator_id character varying(255);
ALTER TABLE role_transaction
    ADD Column if not exists aggregator_id character varying(255);
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'unique_roles_columns_constraint'
    ) THEN
ALTER TABLE role
    ADD CONSTRAINT unique_roles_columns_constraint UNIQUE (role_name, level_id,aggregator_id);
END IF;
END $$;
