DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'unique_role_role_modules'
    ) THEN
ALTER TABLE role_modules
    ADD CONSTRAINT unique_role_role_modules UNIQUE (role_id);
END IF;
END $$;
